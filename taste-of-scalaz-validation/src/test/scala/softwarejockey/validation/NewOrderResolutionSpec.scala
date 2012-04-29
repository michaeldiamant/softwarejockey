package softwarejockey.validation

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import util.Random._
import org.specs2.execute.{Result, Success}

class NewOrderResolutionSpec extends Specification with Mockito with NewOrderResolutionErrors {
  isolated

  "NewOrderResolver" should {

    implicit def examplesToResult(examples: List[org.specs2.specification.Example]) =
      examples.foldLeft[Result](Success(m = "", expNb = 0))((previousResult, example) => example.body().and(previousResult))

    val mockSymbolDao = mock[SymbolDao]
    val mockMarketMakerDao = mock[MarketMakerDao]
    val mockMarketTakerDao = mock[MarketTakerDao]

    val javaStyleValidator = new JavaStyleNewOrderResolver {
      val symbolDao = mockSymbolDao
      val marketMakerDao = mockMarketMakerDao
      val marketTakerDao = mockMarketTakerDao
    }

    val validationStyleValidator = new MonadicStyleNewOrderResolver {
      val symbolDao = mockSymbolDao
      val marketMakerDao = mockMarketMakerDao
      val marketTakerDao = mockMarketTakerDao
    }

    def randomNewOrder = NewOrder(
      accountId = nextString(10),
      symbolName = nextString(6),
      marketTakerId = nextString(10),
      marketMakerId = nextString(10),
      quantity = nextInt(100) + 1,
      price = BigDecimal((nextDouble() + 1) * 50),
      side = Buy
    )

    def randomAccount = TradingAccount(id = nextString(10))

    def randomSymbol = Symbol(name = nextString(6))

    def verifyJavaStyleResolutionError(expectedError: String)(implicit order: NewOrder) =
      javaStyleValidator.resolve(order).fold(
        error => error must_== expectedError,
        resolvedOrder => failure("Expected resolution error")
      )

    def verifyValidationStyleResolutionError(expectedError: String)(implicit order: NewOrder) =
      validationStyleValidator.resolve(order).fold(
        error => error must_== expectedError,
        resolvedOrder => failure("Expected resolution error")
      )

    val UsingJavaStyleResolver = "using Java style resolver"
    val UsingMonadicStyleResolver = "using monadic style resolver"

    "resolve valid new order" >> {
      val order = randomNewOrder

      val symbol = Symbol(name = order.symbolName)
      val marketMaker = MarketMaker(externalId = order.marketMakerId)
      val account = TradingAccount(order.accountId)
      val marketTaker = MarketTaker(
        externalId = order.marketTakerId,
        accounts = (List.fill(10)(randomAccount) ++ List(account)).toSet,
        symbols = (List.fill(10)(randomSymbol) ++ List(symbol)).toSet
      )

      mockSymbolDao.findByName(order.symbolName) returns Some(symbol)
      mockMarketTakerDao.findByExternalId(order.marketTakerId) returns Some(marketTaker)
      mockMarketMakerDao.findByExternalId(order.marketMakerId) returns Some(marketMaker)


      def verifyOrder(resolvedOrder: ResolvedNewOrder) = {
        resolvedOrder.account must_== account
        resolvedOrder.symbol must_== symbol
        resolvedOrder.marketMaker must_== marketMaker
        resolvedOrder.price must_== order.price
        resolvedOrder.quantity must_== order.quantity
        resolvedOrder.side must_== order.side
      }

      "using Java style resolver" >> {
        javaStyleValidator.resolve(order).fold(
          error => failure("Unexpected error = " + error),
          resolvedOrder => verifyOrder(resolvedOrder)
        )
      }

      "using Validation style resolver" >> {
        validationStyleValidator.resolve(order).fold(
          error => failure("Unexpected error = " + error),
          resolvedOrder => verifyOrder(resolvedOrder)
        )
      }
    }

    "fail resolution provided unknown symbol" >> {
      implicit val order = randomNewOrder

      mockSymbolDao.findByName(order.symbolName) returns None

      UsingJavaStyleResolver >> {
        verifyJavaStyleResolutionError(expectedError = UnknownSymbol)
      }

      UsingMonadicStyleResolver >> {
        verifyValidationStyleResolutionError(expectedError = UnknownSymbol)
      }
    }

    "fail resolution provided unknown market taker ID" >> {
      implicit val order = randomNewOrder

      mockSymbolDao.findByName(order.symbolName) returns Some(randomSymbol)
      mockMarketTakerDao.findByExternalId(order.marketTakerId) returns None

      UsingJavaStyleResolver >> {
        verifyJavaStyleResolutionError(expectedError = UnknownMarketTakerId)
      }

      UsingMonadicStyleResolver >> {
        verifyValidationStyleResolutionError(expectedError = UnknownMarketTakerId)
      }
    }

    "fail resolution provided unauthorized account" >> {
      implicit val order = randomNewOrder

      mockSymbolDao.findByName(order.symbolName) returns Some(randomSymbol)
      val marketTaker = MarketTaker(
        externalId = order.marketTakerId,
        accounts = List.fill(10)(randomAccount).toSet,
        symbols = List.fill(10)(randomSymbol).toSet
      )
      mockMarketTakerDao.findByExternalId(order.marketTakerId) returns Some(marketTaker)

      UsingJavaStyleResolver >> {
        verifyJavaStyleResolutionError(expectedError = UnauthorizedAccount)
      }

      UsingMonadicStyleResolver >> {
        verifyValidationStyleResolutionError(expectedError = UnauthorizedAccount)
      }
    }

    "fail resolution provided unknown market maker ID" >> {
      implicit val order = randomNewOrder

      mockSymbolDao.findByName(order.symbolName) returns Some(randomSymbol)
      val marketTaker = MarketTaker(
        externalId = order.marketTakerId,
        accounts = Set(TradingAccount(id = order.accountId)),
        symbols = List.fill(10)(randomSymbol).toSet
      )
      mockMarketTakerDao.findByExternalId(order.marketTakerId) returns Some(marketTaker)
      mockMarketMakerDao.findByExternalId(order.marketMakerId) returns None

      UsingJavaStyleResolver >> {
        verifyJavaStyleResolutionError(expectedError = UnknownMarketMakerId)
      }

      UsingMonadicStyleResolver >> {
        verifyValidationStyleResolutionError(expectedError = UnknownMarketMakerId)
      }
    }

    "fail resolution provided unauthorized symbol" >> {
      implicit val order = randomNewOrder

      mockSymbolDao.findByName(order.symbolName) returns Some(randomSymbol)
      val marketTaker = MarketTaker(
        externalId = order.marketTakerId,
        accounts = Set(TradingAccount(id = order.accountId)),
        symbols = List.fill(10)(randomSymbol).toSet
      )
      mockMarketTakerDao.findByExternalId(order.marketTakerId) returns Some(marketTaker)
      mockMarketMakerDao.findByExternalId(order.marketMakerId) returns Some(MarketMaker(order.marketMakerId))

      UsingJavaStyleResolver >> {
        verifyJavaStyleResolutionError(expectedError = UnauthorizedSymbol)
      }

      UsingMonadicStyleResolver >> {
        verifyValidationStyleResolutionError(expectedError = UnauthorizedSymbol)
      }
    }

    "fail resolution provided invalid quantity" >> {
      implicit val order = randomNewOrder.copy(quantity = 0)

      val symbol = Symbol(name = order.symbolName)
      mockSymbolDao.findByName(order.symbolName) returns Some(symbol)
      val marketTaker = MarketTaker(
        externalId = order.marketTakerId,
        accounts = Set(TradingAccount(id = order.accountId)),
        symbols = Set(symbol)
      )
      mockMarketTakerDao.findByExternalId(order.marketTakerId) returns Some(marketTaker)
      mockMarketMakerDao.findByExternalId(order.marketMakerId) returns Some(MarketMaker(order.marketMakerId))

      UsingJavaStyleResolver >> {
        verifyJavaStyleResolutionError(expectedError = InvalidQuantity)
      }

      UsingMonadicStyleResolver >> {
        verifyValidationStyleResolutionError(expectedError = InvalidQuantity)
      }
    }

    "fail resolution provided invalid price" >> {
      implicit val order = randomNewOrder.copy(price = BigDecimal(-5))

      val symbol = Symbol(name = order.symbolName)
      mockSymbolDao.findByName(order.symbolName) returns Some(symbol)
      val marketTaker = MarketTaker(
        externalId = order.marketTakerId,
        accounts = Set(TradingAccount(id = order.accountId)),
        symbols = Set(symbol)
      )
      mockMarketTakerDao.findByExternalId(order.marketTakerId) returns Some(marketTaker)
      mockMarketMakerDao.findByExternalId(order.marketMakerId) returns Some(MarketMaker(order.marketMakerId))


      UsingJavaStyleResolver >> {
        verifyJavaStyleResolutionError(expectedError = InvalidPrice)
      }

      UsingMonadicStyleResolver >> {
        verifyValidationStyleResolutionError(expectedError = InvalidPrice)
      }
    }
  }
}
