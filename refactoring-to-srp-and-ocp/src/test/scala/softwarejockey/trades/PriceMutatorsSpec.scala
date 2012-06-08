package softwarejockey.trades

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import util.Random._

class PriceMutatorsSpec extends Specification with Mockito {

  "Symbol-based price mutator" should {

    val randomMarkingUpSymbol = nextString(6)
    val randomSymbolMarkup = BigDecimal(nextInt(100) + 1)

    val mockMarkupApplicator = mock[(Side, BigDecimal, BigDecimal) => BigDecimal]

    val mutator = new PriceMutators {
      val markingUpSymbol = randomMarkingUpSymbol
      val symbolMarkup = randomSymbolMarkup
      val markupVolumeThreshold = 0L
      val volumeMarkup = BigDecimal(0)

      override def addMarkup(side: Side, price: BigDecimal, markup: BigDecimal): BigDecimal =
        mockMarkupApplicator(side, price, markup)

    }.symbolBasedPriceMutator(_, _)

    def newRequestedTrade(symbol: String) =
      RequestedTrade(
        id = nextString(10),
        symbol = symbol,
        volume = nextLong(),
        requestedPrice = BigDecimal(nextInt(100) + 1),
        side = nextBoolean() match {
          case true => Buy
          case false => Sell
        }
      )

    "mutate price provided trade with symbol = marking up symbol" >> {
      val trade = newRequestedTrade(symbol = randomMarkingUpSymbol)
      val executionPrice = BigDecimal(nextInt(100) + 1)
      val markedUpExecutionPrice = executionPrice + 5

      mockMarkupApplicator(trade.side, executionPrice, randomSymbolMarkup) returns markedUpExecutionPrice

      mutator(trade, executionPrice) must_== markedUpExecutionPrice
    }

    "leave price unchanged provided trade with symbol != symbol to be marked up" >> {
      val trade = newRequestedTrade(symbol = nextString(5))
      val executionPrice = BigDecimal(nextInt(100) + 1)

      mutator(trade, executionPrice) must_== executionPrice
    }
  }

  "Volume threshold price mutator" should {

    val randomMarkupVolumeThreshold = nextLong()
    val randomVolumeMarkup = BigDecimal(nextInt(100) + 1)

    val mockMarkupApplicator = mock[(Side, BigDecimal, BigDecimal) => BigDecimal]

    val mutator = new PriceMutators {
      val markingUpSymbol = ""
      val symbolMarkup = BigDecimal(0)
      val markupVolumeThreshold = randomMarkupVolumeThreshold
      val volumeMarkup = randomVolumeMarkup

      override def addMarkup(side: Side, price: BigDecimal, markup: BigDecimal): BigDecimal =
        mockMarkupApplicator(side, price, markup)

    }.volumeThresholdPriceMutator(_, _)

    def newRequestedTrade(volume: Long) =
      RequestedTrade(
        id = nextString(10),
        symbol = nextString(6),
        volume = volume,
        requestedPrice = BigDecimal(nextInt(100) + 1),
        side = nextBoolean() match {
          case true => Buy
          case false => Sell
        }
      )

    "mutate price provided trade with volume <= markup volume threshold" >> {
      val trade = newRequestedTrade(volume = randomMarkupVolumeThreshold)
      val executionPrice = BigDecimal(nextInt(100) + 1)
      val markedUpExecutionPrice = executionPrice + 5

      mockMarkupApplicator(trade.side, executionPrice, randomVolumeMarkup) returns markedUpExecutionPrice

      mutator(trade, executionPrice) must_== markedUpExecutionPrice
    }

    "leave price unchanged provided trade with volume > markup volume threshold" >> {
      val trade = newRequestedTrade(volume = randomMarkupVolumeThreshold + 1)
      val executionPrice = BigDecimal(nextInt(100) + 1)

      mutator(trade, executionPrice) must_== executionPrice
    }
  }
}
