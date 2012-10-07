package softwarejockey.prices

import TaggedTypes._

trait Usd

trait Eur

trait ProfitCalculator[T] {

  def calculateProfit(executionPrice: BigDecimal @@ T, requestedPrice: BigDecimal @@ T): BigDecimal @@ T =
    tag(executionPrice - requestedPrice)
}

object ProfitCalculatorExample extends App {

  val usdProfitCalculator = new ProfitCalculator[Usd] {}

  // Does not compile
  //  usdProfitCalculator.calculateProfit(
  //    executionPrice = tag[Eur](BigDecimal(5)),
  //    requestedPrice = tag[Usd](BigDecimal(5))
  //  )

  val profit = usdProfitCalculator.calculateProfit(
    executionPrice = tag[Usd](BigDecimal(5)),
    requestedPrice = tag[Usd](BigDecimal(5))
  )

  println(profit)
}
