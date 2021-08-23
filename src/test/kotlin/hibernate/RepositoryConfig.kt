package hibernate

import config.Config

/**
 * DB 접속을 위한 설정 정보.
 *
 * @property host DB IP 주소
 * @property port DB 포트
 * @property dbName Database 명
 * @property userName 사용자 명
 * @property password 패스워드
 */
data class RepositoryConfig(
    val host: String,
    val port: Int,
    val dbName: String,
    val userName: String? = null,
    val password: String? = null
) : Config {
    var url = "jdbc:postgresql://$host:$port/$dbName"

    fun setMssqlUrl() {
        url = "jdbc:sqlserver://$host:$port;database=$dbName"
    }
}
