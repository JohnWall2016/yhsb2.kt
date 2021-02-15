package yhsb.qb.net.protocol

/** 养老个人账户总账查询 */
class AccountTotalQuery(
    id: String
) : ParamList(
    "F03.01.19.01",
    linkedMapOf("name" to "ac01"),
    linkedMapOf("aac001" to id)
)