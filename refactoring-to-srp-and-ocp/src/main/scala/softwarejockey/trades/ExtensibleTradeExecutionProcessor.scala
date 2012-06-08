package softwarejockey.trades

import scalaz._
import Scalaz._

trait ExtensibleTradeExecutionProcessor extends TradeExecutionProcessor {

  val requestedTradeRepository: RequestedTradeRepository
  val priceMutators: Set[(RequestedTrade, BigDecimal) => BigDecimal]

  def process(requestId: String, executionPrice: BigDecimal): Option[ExecutedTrade] =
    for {
      requestedTrade <- requestedTradeRepository.findById(requestId)
      reportedPrice <- priceMutators.foldLeft(executionPrice)((price, mutator) => mutator(requestedTrade, price)).some
    } yield
      ExecutedTrade(
        requestId = requestedTrade.id,
        executedPrice = executionPrice,
        reportedPrice = reportedPrice
      )
}
