package services.time

import java.time.Instant

trait ClockService {

  def getCurrentTime: Instant
}
