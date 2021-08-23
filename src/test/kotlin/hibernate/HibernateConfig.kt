package hibernate

import config.Config

/**
 * DB 접속을 위한 설정 정보.
 *
 * @property driverClass Database 드라이버 정보
 * @property dialect Database SQL 방언 정보
 * @property isShowSql Hibernate 가 생성 하는 SQL의 콘솔 출력 설정
 * @property isFormatSql SQL 콘솔 출력의 가독성 설정
 * @property ddlAuto Entity 가 변경 되었을 때 Database 에 반영 방법 설정
 *                   validate, update, create, create-drop
 */

data class HibernateConfig(
    val driverClass: String,
    val dialect: String,
    val isShowSql: Boolean,
    val isFormatSql: Boolean,
    val ddlAuto: String
) : Config
