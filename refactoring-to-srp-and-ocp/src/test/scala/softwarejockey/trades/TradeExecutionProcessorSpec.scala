package softwarejockey.trades

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import util.Random._

class TradeExecutionProcessorSpec extends Specification with Mockito with MarkupApplicator {

  "TradeExecutionProcessor" should {

    val randomMarkingUpSymbol = nextString(6)
    val randomSymbolMarkup = BigDecimal(nextInt(100) + 1)

    val randomMarkupVolumeThreshold = nextLong()
    val randomVolumeMarkup = BigDecimal(nextInt(100) + 1)

    val mockRequestedTradeRepository = mock[RequestedTradeRepository]

    val complectingProcessor = new ComplectingTradeExecutionProcessor {
      val markingUpSymbol = randomMarkingUpSymbol
      val symbolMarkup = randomSymbolMarkup
      val markupVolumeThreshold = randomMarkupVolumeThreshold
      val volumeMarkup = randomVolumeMarkup
      val requestedTradeRepository = mockRequestedTradeRepository
    }

    val mutators = new PriceMutators {
      val markingUpSymbol = randomMarkingUpSymbol
      val symbolMarkup = randomSymbolMarkup
      val markupVolumeThreshold = randomMarkupVolumeThreshold
      val volumeMarkup = randomVolumeMarkup
    }

    val extensibleProcessor = new ExtensibleTradeExecutionProcessor {
      val priceMutators = Set(
        mutators.symbolBasedPriceMutator(_, _),
        mutators.volumeThresholdPriceMutator(_, _)
      )
      val requestedTradeRepository = mockRequestedTradeRepository
    }

    "mutate price based on symbol and volume threshold" >> {
      val requestedTrade = RequestedTrade(
        id = nextString(10),
        symbol = randomMarkingUpSymbol,
        volume = randomMarkupVolumeThreshold,
        requestedPrice = BigDecimal(nextInt(100) + 1),
        side = Buy
      )

      mockRequestedTradeRepository.findById(requestedTrade.id) returns Some(requestedTrade)

      val executionPrice = BigDecimal(nextInt(100) + 1)

      def verifyExecutedTrade(trade: Option[ExecutedTrade]) =
        trade match {
          case Some(t) => {
            t.requestId must_== requestedTrade.id
            t.executedPrice must_== executionPrice
            t.reportedPrice must_== addMarkup(requestedTrade.side, executionPrice, randomSymbolMarkup + randomVolumeMarkup)
          }
          case None => failure("Expected an executed trade")
        }

      "using complecting processor" >> {
        verifyExecutedTrade(complectingProcessor.process(requestedTrade.id, executionPrice))
      }

      "using extensible processor" >> {
        verifyExecutedTrade(extensibleProcessor.process(requestedTrade.id, executionPrice))
      }
    }
  }
}
