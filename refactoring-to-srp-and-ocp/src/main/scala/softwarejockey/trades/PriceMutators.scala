package softwarejockey.trades

import scalaz._
import Scalaz._

trait PriceMutators extends MarkupApplicator {

  val markingUpSymbol: String
  val symbolMarkup: BigDecimal

  val markupVolumeThreshold: Long
  val volumeMarkup: BigDecimal

  def symbolBasedPriceMutator(trade: RequestedTrade, executionPrice: BigDecimal): BigDecimal =
    (trade.symbol == markingUpSymbol)
      .option(addMarkup(trade.side, executionPrice, symbolMarkup))
      .getOrElse(executionPrice)

  def volumeThresholdPriceMutator(trade: RequestedTrade, executionPrice: BigDecimal): BigDecimal =
    (trade.volume <= markupVolumeThreshold)
      .option(addMarkup(trade.side, executionPrice, volumeMarkup))
      .getOrElse(executionPrice)
}
