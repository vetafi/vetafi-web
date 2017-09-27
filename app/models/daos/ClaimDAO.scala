package models.daos

import java.util.UUID

import models.{ Claim, ClaimSubmission }
import reactivemongo.api.commands.{ MultiBulkWriteResult, WriteResult }

import scala.concurrent.Future

trait ClaimDAO {
  def findClaims(userID: UUID): Future[Seq[Claim]]
  def findClaim(userID: UUID, claimID: UUID): Future[Option[Claim]]
  def findIncompleteClaim(userID: UUID): Future[Option[Claim]]
  def create(userID: UUID, key: String): Future[WriteResult]
  def submit(userID: UUID, claimID: UUID, submissions: Seq[ClaimSubmission]): Future[WriteResult]
  def save(userID: UUID, claimID: UUID, claim: Claim): Future[WriteResult]
}
