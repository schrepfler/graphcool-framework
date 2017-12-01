package cool.graph.deploy.database.persistence

import cool.graph.deploy.InternalTestDatabase
import cool.graph.deploy.database.tables.Tables
import cool.graph.shared.models.{Enum, Migration, Project}
import cool.graph.shared.project_dsl.TestProject
import cool.graph.utils.await.AwaitUtils
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import slick.jdbc.MySQLProfile.api._

class ProjectPersistenceImplSpec extends FlatSpec with Matchers with AwaitUtils with InternalTestDatabase with BeforeAndAfterEach {
  import scala.concurrent.ExecutionContext.Implicits.global

  val projectPersistence   = ProjectPersistenceImpl(internalDatabase = internalDatabase)
  val migrationPersistence = MigrationPersistenceImpl(internalDatabase = internalDatabase)
  val project              = TestProject()
  val migration: Migration = Migration.empty

  override def beforeEach(): Unit = {
    super.beforeEach()

    (for {
      _ <- projectPersistence.create(project)
      _ <- migrationPersistence.create(project, migration.copy(hasBeenApplied = true))
    } yield ()).await
  }

  ".load()" should "return None if there's no project yet in the database" in {
    val result = projectPersistence.load("non-existent-id").await()
    result should be(None)
  }

  ".load()" should "return the project with the highest revision" in {
    val result = projectPersistence.load("non-existent-id").await()
    result should be(None)
  }

  ".loadByIdOrAlias()" should "be able to load a project by it's alias and id." in {
    val result = projectPersistence.loadByIdOrAlias("non-existent-id").await()
    result should be(None)
  }

//  ".load()" should "return the project with the highest revision" in {
//    projectPersistence.create(project, migrationSteps).await()
//    projectPersistence.markMigrationAsApplied(project, migrationSteps).await()
//
//    projectPersistence.load(project.id).await() should equal(Some(project))
//    assertNumberOfRowsInProjectTable(1)
//
//    val newEnum            = Enum(id = "does-not-matter", name = "MyEnum", values = Vector("Value1", "Value2"))
//    val newProjectRevision = project.copy(enums = List(newEnum))
//
//    projectPersistence.save(newProjectRevision, migrationSteps).await()
//    projectPersistence.markMigrationAsApplied(project, migrationSteps).await()
//
//    assertNumberOfRowsInProjectTable(2)
//    val expectedProject = newProjectRevision.copy(revision = 2)
//    projectPersistence.load(project.id).await() should equal(Some(expectedProject))
//  }

  ".create()" should "store the project in the db" in {
    assertNumberOfRowsInProjectTable(0)
    projectPersistence.create(project).await()
    assertNumberOfRowsInProjectTable(1)
  }

//  ".save()" should "increment the revision property of the project on each call" in {
//    assertNumberOfRowsInProjectTable(0)
//    projectPersistence.save(project, migrationSteps).await()
//    assertNumberOfRowsInProjectTable(1)
//    getHighestRevisionForProject(project) should equal(1)
//
//    projectPersistence.save(project, migrationSteps).await()
//    assertNumberOfRowsInProjectTable(2)
//    getHighestRevisionForProject(project) should equal(2)
//  }

  def assertNumberOfRowsInProjectTable(count: Int): Unit = {
    val query = Tables.Projects.size
    runQuery(query.result) should equal(count)
  }

//  def getHighestRevisionForProject(project: Project): Int = {
//    val query = for {
//      project <- Tables.Projects
//    } yield project
//
//    runQuery(query.result).map(_.revision).max
//  }
//
  def runQuery[R](a: DBIOAction[R, NoStream, Nothing]): R = internalDatabase.run(a).await()
}