package services.time

import java.time.Instant

class SystemClockService extends ClockService {

  override def getCurrentTime: Instant = Instant.now()
}
