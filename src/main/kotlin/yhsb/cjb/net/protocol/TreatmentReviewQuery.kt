package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName
import yhsb.base.net.HttpSocket
import yhsb.base.util.Config
import java.math.BigDecimal
import java.net.URLEncoder

/** 待遇复核查询 */
class TreatmentReviewQuery(
    idCard: String = "",
    reviewState: String = "0",
    reviewStartTime: String = "",
    reviewEndTime: String = "",
    page: Int = 1,
    pageSize: Int = 500,
    sorting: Map<String, String> = mapOf(
        "dataKey" to "aaa027",
        "sortDirection" to "ascending"
    )
) : PageRequest(
    "dyfhQuery", page, pageSize, sorting = sorting
) {
    val aaf013 = ""
    val aaf030 = ""

    @SerializedName("aae016")
    val reviewState = reviewState

    val aae011 = ""
    val aae036 = ""
    val aae036s = ""
    val aae014 = ""

    @SerializedName("aae015")
    val reviewStartTime = reviewStartTime

    @SerializedName("aae015s")
    val reviewEndTime = reviewEndTime

    val aac009 = ""
    val aac003 = ""

    @SerializedName("aac002")
    val idCard = idCard

    data class Item(
        /** 个人编号 */
        @SerializedName("aac001")
        val pid: Int,

        /** 身份证号码 */
        @SerializedName("aac002")
        val idCard: String,

        @SerializedName("aac003")
        val name: String,

        /** 行政区划 */
        @SerializedName("aaa027")
        val xzqh: String,

        /** 单位名称 */
        @SerializedName("aaa129")
        val agency: String,

        /** 月养老金 */
        @SerializedName("aic166")
        val payAmount: BigDecimal,

        /** 财务月份 */
        @SerializedName("aae211")
        val accountMonth: Int,

        /** 实际待遇开始月份 */
        @SerializedName("aic160")
        val payMonth: Int,

        val bz: String,

        val aaz170: Int,
        val aaz159: Int,
        val aaz157: Int
    ) {
        val treatmentInfoUrl: String
            get() =
                "/hncjb/reports?method=htmlcontent&name=yljjs&" +
                        "aaz170=${escape(aaz170)}&aaz159=${escape(aaz159)}&aac001=${escape(pid)}&" +
                        "aaz157=${escape(aaz157)}&aaa129=${escape(agency)}&aae211=${escape(accountMonth)}"

        fun getTreatmentInfoMatch(): MatchResult? {
            return HttpSocket(
                Config.cjbSession.getString("host"),
                Config.cjbSession.getInt("port"),
            ).use {
                val content = it.getHttp(treatmentInfoUrl).replace("""[\r\n\t]""".toRegex(), "")
                rePaymentInfo.toRegex().find(content)
            }
        }
    }

    companion object {
        private fun escape(o: Any) = URLEncoder.encode(o.toString(), "UTF-8")

        private val rePaymentInfo: String = """<tr id="75">
        <td height="32" align="center" id="3">姓名</td>
        <td align="center" id="4">性别</td>
        <td align="center" colspan="3" id="5">身份证</td>
        <td align="center" colspan="3" id="6">困难级别</td>
        <td align="center" colspan="3" id="7">户籍所在地</td>
        <td align="center" colspan="3" id="8">所在地行政区划编码</td>
      </tr>
      <tr class="detail" component="detail" id="76">
        <td height="39" align="center" id="9">(.+?)</td>
        <td align="center" colspan="" id="10">(.+?)</td>
        <td align="center" colspan="3" id="11">(.+?)</td>
        <td align="center" colspan="3" id="12">(.+?)</td>
        <td align="center" colspan="3" id="13"(?:/>|>(.+?)</td>)
        <td align="center" colspan="3" id="14">(.+?)</td>
      </tr>
      <tr id="77">
        <td height="77" align="center" rowspan="2" id="15">缴费起始年月</td>
        <td align="center" rowspan="2" id="16">累计缴费年限</td>
        <td align="center" rowspan="2" colspan="2" id="17">个人账户累计存储额</td>
        <td height="25" align="center" colspan="9" id="18">其中</td>
      </tr>
      <tr id="78">
        <td height="30" align="center" id="19">个人缴费</td>
        <td align="center" id="20">省级补贴</td>
        <td align="center" id="21">市级补贴</td>
        <td align="center" id="22">县级补贴</td>
        <td align="center" id="23">集体补助</td>
        <td align="center" id="24">被征地补助</td>
        <td align="center" id="24">退捕渔民补助</td>
        <td align="center" id="25">政府代缴</td>
        <td align="center" id="26">利息</td>
      </tr>
      <tr class="detail" component="detail" id="79">
        <td height="40" align="center" id="27">(.+?)</td>
        <td align="center" id="28">(.+?)</td>
        <td align="center" colspan="2" id="29">(.+?)</td>
        <td align="center" id="30">(.+?)</td>
        <td align="center" id="31">(.+?)</td>
        <td align="center" id="32">(.+?)</td>
        <td align="center" id="33">(.+?)</td>
        <td align="center" id="34">(.+?)</td>
        <td align="center" id="35">(.+?)</td>
        <td align="center" id="35">(.+?)</td>
        <td align="center" id="36">(.+?)</td>
        <td align="center" id="37">(.+?)</td>
      </tr>
      <tr id="80">
        <td align="center" rowspan="2" id="38">
          <p>领取养老金起始时间</p>
        </td>
        <td align="center" rowspan="2" id="39">月养老金</td>
        <td height="29" align="center" colspan="5" id="40">其中：基础养老金</td>
        <td align="center" colspan="6" id="41">个人账户养老金</td>
      </tr>
      <tr id="81">
        <td height="31" align="center" id="42">国家补贴</td>
        <td height="31" align="center" id="43">省级补贴</td>
        <td align="center" id="44">市级补贴</td>
        <td align="center" id="45">县级补贴</td>
        <td align="center" id="46">加发补贴</td>
        <td align="center" id="47">个人实缴部分</td>
        <td align="center" id="48">缴费补贴部分</td>
        <td align="center" id="49">集体补助部分</td>
        <td align="center" id="50">被征地补助部分</td>
        <td align="center" id="50">退捕渔民补助部分</td>
        <td align="center" id="51">政府代缴部分</td>
      </tr>
      <tr class="detail" component="detail" id="82">
        <td height="40" align="center" id="52">(.+?)</td>
        <td align="center" id="53">(.+?)</td>
        <td align="center" id="54">(.+?)</td>
        <td align="center" id="55">(.+?)</td>
        <td align="center" id="56">(.+?)</td>
        <td align="center" id="57">(.+?)</td>
        <td align="center" id="58">(.+?)</td>
        <td align="center" id="59">(.+?)</td>
        <td align="center" id="60">(.+?)</td>
        <td align="center" id="61">(.+?)</td>
        <td align="center" id="62">(.+?)</td>
        <td align="center" id="62">(.+?)</td>
        <td align="center" id="63">(.+?)</td>
      </tr>""".replace("""[\r\n\t]""".toRegex(), "")
    }
}