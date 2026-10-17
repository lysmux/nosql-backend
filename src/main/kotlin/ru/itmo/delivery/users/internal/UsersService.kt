package ru.itmo.delivery.users.internal

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.itmo.delivery.auth.api.exceptions.InvalidCredentialsException
import ru.itmo.delivery.shared.exceptions.ValidationException
import ru.itmo.delivery.users.api.CourierAvailability
import ru.itmo.delivery.users.api.Users
import ru.itmo.delivery.users.api.exceptions.CourierModeConflictException
import ru.itmo.delivery.users.api.exceptions.EmailConflictException
import ru.itmo.delivery.users.api.exceptions.UserNotFoundException
import ru.itmo.delivery.users.api.models.User
import ru.itmo.delivery.users.internal.adapters.outbound.dao.UsersDAO
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class UsersService(
    private val dao: UsersDAO,
    private val passwordEncoder: PasswordEncoder,
    private val courierAvailability: CourierAvailability,
) : Users {
    private val dummyHash = hash(UUID.randomUUID().toString())

    override fun register(name: String, email: String, password: String): User {
        val normalizedEmail = normalizeEmail(email)
        val user = User(
            id = UUID.randomUUID(),
            name = checkName(name),
            email = checkEmail(normalizedEmail),
            courierModeEnabled = false,
            createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS),
        )

        checkPassword(password)
        if (dao.existsByEmail(normalizedEmail)) throw EmailConflictException()

        try {
            dao.save(user, hash(password))
        } catch (_: DataIntegrityViolationException) {
            throw EmailConflictException()
        }

        return user
    }

    override fun authenticate(email: String, password: String): UUID {
        if (password.length > MAX_PASSWORD_LENGTH) throw InvalidCredentialsException()

        val credentials = dao.findCredentials(normalizeEmail(email))
        val verified = passwordEncoder.matches(password, credentials?.passwordHash ?: dummyHash)

        if (credentials == null || !verified) throw InvalidCredentialsException()
        return credentials.userId
    }

    override fun get(id: UUID): User? = dao.get(id)

    override fun getAll(ids: Collection<UUID>): Map<UUID, User> =
        if (ids.isEmpty()) emptyMap() else dao.getAll(ids)

    override fun exists(id: UUID): Boolean = dao.exists(id)

    override fun courierIds(): List<UUID> = dao.findCourierIds()

    override fun setCourierMode(userId: UUID, enabled: Boolean): User {
        val user = dao.get(userId) ?: throw UserNotFoundException(userId)

        if (user.courierModeEnabled == enabled) return user
        if (!enabled && courierAvailability.existsActiveAssignment(userId)) throw CourierModeConflictException()

        return dao.updateCourierMode(userId, enabled) ?: throw UserNotFoundException(userId)
    }

    private fun hash(password: String): String = checkNotNull(passwordEncoder.encode(password))

    private fun normalizeEmail(email: String) = email.trim().lowercase()

    private fun checkName(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.length > MAX_NAME_LENGTH) {
            throw ValidationException("Имя должно быть от 1 до $MAX_NAME_LENGTH символов")
        }

        return trimmed
    }

    private fun checkEmail(email: String): String {
        if (email.length > MAX_EMAIL_LENGTH || !EMAIL_PATTERN.matches(email)) {
            throw ValidationException("Некорректный email")
        }
        return email
    }

    private fun checkPassword(password: String) {
        if (password.length !in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH) {
            throw ValidationException("Пароль должен быть от $MIN_PASSWORD_LENGTH до $MAX_PASSWORD_LENGTH символов")
        }
    }

    private companion object {
        const val MAX_NAME_LENGTH = 120
        const val MAX_EMAIL_LENGTH = 254
        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_PASSWORD_LENGTH = 128
        val EMAIL_PATTERN = Regex("""^[^@\s]+@[^@\s]+\.[^@\s]+${'$'}""")
    }
}
