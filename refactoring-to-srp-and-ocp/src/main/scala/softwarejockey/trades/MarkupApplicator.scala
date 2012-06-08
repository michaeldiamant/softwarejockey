package softwarejockey.trades

trait MarkupApplicator {

  def addMarkup(side: Side, price: BigDecimal, markup: BigDecimal) =
    side match {
      case Buy => price + markup
      case Sell => price - markup
    }
}
