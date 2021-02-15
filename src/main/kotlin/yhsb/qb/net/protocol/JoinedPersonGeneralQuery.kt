package yhsb.qb.net.protocol

import yhsb.base.xml.AttrNode

class JoinedPersonGeneralQuery(
    id: String,
    agencyCode: String
) : AddSql(
    "F27.00.01",
    "F27.02.01",
    "ac01.aac001 = &apos;${id}&apos;"
) {
    @AttrNode("para", "aab034")
    val agencyCode = agencyCode
}