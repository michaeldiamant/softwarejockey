package softwarejockey.prices

case class UsdPrice(value: BigDecimal) {

  def +(p: UsdPrice) = UsdPrice(value + p.value)

  def -(p: UsdPrice) = UsdPrice(value - p.value)
}

case class EurPrice(value: BigDecimal) {

  def +(p: EurPrice) = EurPrice(value + p.value)

  def -(p: EurPrice) = EurPrice(value - p.value)
}


