package vcmsa.projects.fkj_consultants.activities

data class User(
    var userId: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var username: String = "",
    var email: String = "",
    var address: String = "",
    var createdAt: Long = 0L,
    var lastLogin: Long = 0L
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "", "", 0L, 0L)
}