package lunatech.lunchplanner.controllers

import java.util.UUID

import javax.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.Dish
import lunatech.lunchplanner.models._
import lunatech.lunchplanner.services.{
  DishService,
  MenuDishService,
  UserService
}
import lunatech.lunchplanner.viewModels.{DishForm, ListDishesForm}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import play.api.{Configuration, Environment}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DishController @Inject()(
    userService: UserService,
    dishService: DishService,
    menuDishService: MenuDishService,
    val controllerComponents: ControllerComponents,
    val environment: Environment,
    val configuration: Configuration)(implicit val connection: DBConnection)
    extends BaseController
    with Secured
    with I18nSupport {

  def getAllDishes: Action[AnyContent] = adminAction.async { implicit request =>
    for {
      currentUser <- userService.getByEmailAddress(request.email)
      dishes <- dishService.getAll.map(_.toArray)
    } yield
      Ok(
        views.html.admin.dish.dishes(
          getCurrentUser(currentUser, isAdmin = true, request.email),
          ListDishesForm.listDishesForm,
          dishes))
  }

  def getNewDish: Action[AnyContent] = adminAction.async { implicit request =>
    for {
      currentUser <- userService.getByEmailAddress(request.email)
    } yield
      Ok(
        views.html.admin.dish.newDish(
          getCurrentUser(currentUser, isAdmin = true, request.email),
          DishForm.dishForm))
  }

  def createNewDish: Action[AnyContent] = adminAction.async {
    implicit request =>
      DishForm.dishForm.bindFromRequest
        .fold(
          formWithErrors => {
            for {
              currentUser <- userService.getByEmailAddress(request.email)
            } yield
              BadRequest(
                views.html.admin.dish.newDish(
                  getCurrentUser(currentUser, isAdmin = true, request.email),
                  formWithErrors))
          },
          dishForm =>
            dishService
              .add(getDish(dishForm))
              .map(
                _ =>
                  Redirect(
                    lunatech.lunchplanner.controllers.routes.DishController.getAllDishes)
                    .flashing("success" -> "New dish created!"))
        )
  }

  def getDishDetails(uuid: UUID): Action[AnyContent] = adminAction.async {
    implicit request =>
      for {
        currentUser <- userService.getByEmailAddress(request.email)
        dish <- dishService.getByUuid(uuid)
      } yield
        Ok(
          views.html.admin.dish.dishDetails(
            getCurrentUser(currentUser, isAdmin = true, request.email),
            DishForm.dishForm,
            dish))
  }

  def saveDishDetails(uuid: UUID): Action[AnyContent] = adminAction.async {
    implicit request =>
      DishForm.dishForm.bindFromRequest
        .fold(
          formWithErrors => {
            for {
              currentUser <- userService.getByEmailAddress(request.email)
              dish <- dishService.getByUuid(uuid)
            } yield
              BadRequest(
                views.html.admin.dish.dishDetails(
                  getCurrentUser(currentUser, isAdmin = true, request.email),
                  formWithErrors,
                  dish))
          },
          dishForm => {
            dishService
              .update(uuid, getDish(dishForm))
              .map(
                _ =>
                  Redirect(
                    lunatech.lunchplanner.controllers.routes.DishController.getAllDishes)
                    .flashing("success" -> "Dish updated!"))
          }
        )
  }

  def deleteDishes(): Action[AnyContent] = adminAction.async {
    implicit request =>
      ListDishesForm.listDishesForm.bindFromRequest
        .fold(
          formWithErrors => {
            for {
              currentUser <- userService.getByEmailAddress(request.email)
              dishes <- dishService.getAll.map(_.toArray)
            } yield
              BadRequest(
                views.html.admin.dish
                  .dishes(currentUser.get, formWithErrors, dishes))
          },
          dishData =>
            Future
              .sequence(
                dishData.listUuids.map(uuid => deleteDishAndDependencies(uuid)))
              .map(_ =>
                Redirect(
                  lunatech.lunchplanner.controllers.routes.DishController.getAllDishes)
                  .flashing("success" -> "Dish(es) deleted!"))
        )
  }

  def deleteDish(uuid: UUID): Action[AnyContent] = adminAction.async {
    implicit request =>
      deleteDishAndDependencies(uuid)
        .map(_ =>
          Redirect(
            lunatech.lunchplanner.controllers.routes.DishController.getAllDishes)
            .flashing("success" -> "Dish deleted!"))
  }

  private def deleteDishAndDependencies(uuid: UUID) = {
    for {
      _ <- menuDishService.deleteByDishUuid(uuid)
      result <- dishService.delete(uuid)
    } yield result
  }

  private def getDish(dishForm: DishForm) =
    Dish(
      name = dishForm.name.normalize,
      description = dishForm.description.normalize,
      isVegetarian = dishForm.isVegetarian,
      hasSeaFood = dishForm.hasSeaFood,
      hasPork = dishForm.hasPork,
      hasBeef = dishForm.hasBeef,
      hasChicken = dishForm.hasChicken,
      isGlutenFree = dishForm.isGlutenFree,
      hasLactose = dishForm.hasLactose,
      remarks = dishForm.remarks.map(_.normalize)
    )
}
