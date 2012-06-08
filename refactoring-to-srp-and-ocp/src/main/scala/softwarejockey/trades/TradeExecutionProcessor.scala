package softwarejockey.trades

trait TradeExecutionProcessor {

  def process(requestId: String, executionPrice: BigDecimal): Option[ExecutedTrade]
}
