package testentity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class SubEntity(
    @Id
    @GeneratedValue
    var id: Int? = null
)
