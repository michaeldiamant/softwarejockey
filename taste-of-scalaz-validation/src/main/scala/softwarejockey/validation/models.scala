package softwarejockey.validation

case class MarketMaker(externalId: String)

case class MarketTaker(externalId: String, accounts: Set[TradingAccount], symbols: Set[Symbol])

case class TradingAccount(id: String)

case class Symbol(name: String)

sealed trait OrderSide

object Buy extends OrderSide

object Sell extends OrderSide

case class NewOrder(accountId: String, symbolName: String, marketTakerId: String, marketMakerId: String, quantity: Int,
                    price: BigDecimal, side: OrderSide)

case class ResolvedNewOrder(account: TradingAccount, symbol: Symbol, marketMaker: MarketMaker, quantity: Int,
                            price: BigDecimal, side: OrderSide)
