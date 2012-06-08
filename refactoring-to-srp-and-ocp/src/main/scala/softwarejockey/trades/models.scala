package softwarejockey.trades

trait Side

object Buy extends Side
object Sell extends Side

case class RequestedTrade(id: String, symbol: String, volume: Long,
                          requestedPrice: BigDecimal, side: Side)

case class ExecutedTrade(requestId: String, executedPrice: BigDecimal, reportedPrice: BigDecimal)
