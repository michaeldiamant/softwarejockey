package softwarejockey.orderresolution

import scalaz._
import Scalaz._

trait NewOrderResolutionErrors {

  val UnknownSymbol = "Unknown symbol"
  val UnknownMarketTakerId = "Unknown market taker ID"
  val UnauthorizedAccount = "Account unauthorized for market taker"
  val UnknownMarketMakerId = "Unknown market maker ID"
  val UnauthorizedSymbol = "Unauthorized symbol"
  val InvalidQuantity = "Quantity must be > 0"
  val InvalidPrice = "Price must be > 0"
}

trait JavaStyleNewOrderResolver extends NewOrderResolutionErrors {

  val symbolDao: SymbolDao
  val marketTakerDao: MarketTakerDao
  val marketMakerDao: MarketMakerDao

  def resolve(order: NewOrder): Either[String, ResolvedNewOrder] = {
    val symbol = symbolDao.findByName(order.symbolName)
    if (symbol.isEmpty) {
      return Left(UnknownSymbol)
    }

    val marketTaker = marketTakerDao.findByExternalId(order.marketTakerId)
    if (marketTaker.isEmpty) {
      return Left(UnknownMarketTakerId)
    }

    val account = marketTaker.get.accounts.find(_.id == order.accountId)
    if (account.isEmpty) {
      return Left(UnauthorizedAccount)
    }

    val marketMaker = marketMakerDao.findByExternalId(order.marketMakerId)
    if (marketMaker.isEmpty) {
      return Left(UnknownMarketMakerId)
    }

    if (!marketTaker.get.symbols.exists(_ == symbol.get)) {
      return Left(UnauthorizedSymbol)
    }

    if (order.quantity <= 0) {
      return Left(InvalidQuantity)
    }

    if (order.price <= 0) {
      return Left(InvalidPrice)
    }

    Right(
      ResolvedNewOrder(
        account = account.get,
        symbol = symbol.get,
        marketMaker = marketMaker.get,
        price = order.price,
        quantity = order.quantity,
        side = order.side
      )
    )
  }
}

trait MonadicStyleNewOrderResolver extends NewOrderResolutionErrors {

  val symbolDao: SymbolDao
  val marketTakerDao: MarketTakerDao
  val marketMakerDao: MarketMakerDao

  def resolve(order: NewOrder): Validation[String, ResolvedNewOrder] =
    for {
      symbol <- symbolDao.findByName(order.symbolName)
        .toSuccess(UnknownSymbol)
      marketTaker <- marketTakerDao.findByExternalId(order.marketTakerId)
        .toSuccess(UnknownMarketTakerId)
      account <- marketTaker.accounts.find(_.id == order.accountId)
        .toSuccess(UnauthorizedAccount)
      marketMaker <- marketMakerDao.findByExternalId(order.marketMakerId)
        .toSuccess(UnknownMarketMakerId)
      _ <- marketTaker.symbols.find(_ == symbol)
        .toSuccess(UnauthorizedSymbol)
      _ <- (order.quantity > 0).option(order.quantity)
        .toSuccess(InvalidQuantity)
      _ <- (order.price > 0).option(order.price)
        .toSuccess(InvalidPrice)
    } yield
      ResolvedNewOrder(
        account = account,
        symbol = symbol,
        marketMaker = marketMaker,
        price = order.price,
        quantity = order.quantity,
        side = order.side
      )
}
