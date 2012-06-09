package softwarejockey.trades

import scalaz._
import Scalaz._

trait ComplectingTradeExecutionProcessor extends TradeExecutionProcessor with MarkupApplicator {

  val requestedTradeRepository: RequestedTradeRepository
  val markingUpSymbol: String
  val symbolMarkup: BigDecimal
  val markupVolumeThreshold: Long
  val volumeMarkup: BigDecimal

  def process(requestId: String, executionPrice: BigDecimal): Option[ExecutedTrade] =
    requestedTradeRepository.findById(requestId)
      .map(requestedTrade => {
      val markedUpSymbolExecutionPrice = (requestedTrade.symbol == markingUpSymbol)
        .option(addMarkup(requestedTrade.side, executionPrice, symbolMarkup))
        .getOrElse(executionPrice)

      val markedUpVolumeExecutionPrice = (requestedTrade.volume <= markupVolumeThreshold)
        .option(addMarkup(requestedTrade.side, markedUpSymbolExecutionPrice, volumeMarkup))
        .getOrElse(markedUpSymbolExecutionPrice)

      ExecutedTrade(
        requestId = requestedTrade.id,
        executedPrice = executionPrice,
        reportedPrice = markedUpVolumeExecutionPrice
      )
    })
}
