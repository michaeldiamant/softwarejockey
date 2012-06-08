package softwarejockey.trades

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import util.Random._

class ExtensibleTradeExecutionProcessorSpec extends Specification with Mockito {

  "ExtensibleTradeExecutionProcessor" should {

    def increasingPriceMutator(increaseFactor: BigDecimal) =
      (trade: RequestedTrade, executionPrice: BigDecimal) => executionPrice + increaseFactor

    val mockRequestedTradeRepository = mock[RequestedTradeRepository]

    val priceIncreaseFactor = 5
    val increasingPriceMutators = List.fill(nextInt(100) + 1)(increasingPriceMutator(priceIncreaseFactor)).toSet

    val processor = new ExtensibleTradeExecutionProcessor {
      val requestedTradeRepository = mockRequestedTradeRepository
      val priceMutators = increasingPriceMutators
    }

    def randomRequestedTrade =
      RequestedTrade(
        id = nextString(10),
        symbol = nextString(6),
        volume = nextLong(),
        requestedPrice = BigDecimal(nextInt()),
          side = nextBoolean() match {
          case true => Buy
          case false => Sell
        }
      )

    "return None provided request ID that is not associated with RequestedTrade" >> {
      val trade = randomRequestedTrade

      mockRequestedTradeRepository.findById(trade.id) returns None

      processor.process(trade.id, BigDecimal(nextInt())) match {
        case Some(executedTrade) => failure("Expected None")
        case None => success
      }
    }

    "apply price mutators to provided execution price to generate reported price" >> {
      val trade = randomRequestedTrade

      mockRequestedTradeRepository.findById(trade.id) returns Some(trade)

      val executionPrice = BigDecimal(nextInt(100) + 1)

      processor.process(trade.id, executionPrice) match {
        case Some(executedTrade) => {
          executedTrade.requestId must_== trade.id
          executedTrade.executedPrice must_== executionPrice
          executedTrade.reportedPrice must_== executionPrice + increasingPriceMutators.size * priceIncreaseFactor
        }
        case None => failure("Expected an executed trade")
      }
    }
  }
}
