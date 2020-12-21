package com.upgrad.FoodOrderingApp.api.controller;
import com.upgrad.FoodOrderingApp.api.model.PaymentListResponse;
import com.upgrad.FoodOrderingApp.api.model.PaymentResponse;
import com.upgrad.FoodOrderingApp.service.business.PaymentService;
import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;
@CrossOrigin
@RestController
@RequestMapping("/payment")
public class PaymentController {

  @Autowired PaymentService paymentService;

  @CrossOrigin
  @RequestMapping(
      path = "",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PaymentListResponse> getPaymentModes() {

    // Fetch all payment modes as a list of Payment Entities from the database
    List<PaymentEntity> paymentEntities = paymentService.getAllPaymentMethods();

    // Map list of payment entities to Payment List Response object
    PaymentListResponse response = new PaymentListResponse();
    paymentEntities.forEach(
        paymentEntity ->
            response.addPaymentMethodsItem(
                new PaymentResponse()
                    .id(UUID.fromString(paymentEntity.getUuid()))
                    .paymentName(paymentEntity.getPaymentName())));

    // Return response with right HttpStatus
    if (response.getPaymentMethods().isEmpty()) {
      return new ResponseEntity<PaymentListResponse>(response, HttpStatus.NO_CONTENT);
    } else {
      return new ResponseEntity<PaymentListResponse>(response, HttpStatus.OK);
    }
  }
}
 178  ...App-api/src/main/java/com/upgrad/FoodOrderingApp/api/controller/RestaurantController.java 
@@ -158,182 +158,4 @@
        restaurantListResponse.setRestaurants(restaurantList);
        return new ResponseEntity<>(restaurantListResponse, HttpStatus.OK);
    }

    /**
     * This method  takes category as the input which if served by a restaurant,the restaurant is included
     * in the return list of restaurants
     *
     * @return ResponseEntity with list of all of the Restaurants
     * @throws CategoryNotFoundException if the name is empty
     */
    @CrossOrigin
    @RequestMapping(path = "/category/{category_id}", method = RequestMethod.GET)
    public ResponseEntity<RestaurantListResponse> getRestaurantByCategory(
            @PathVariable("category_id") String categoryId) throws CategoryNotFoundException {
        List<RestaurantList> restaurantList = new ArrayList<RestaurantList>();
        // Get Restaurants information
        List<RestaurantEntity> restaurantEntityList =
                restaurantService.restaurantByCategory(categoryId);

        // transform restaurant information into response objects
        for (RestaurantEntity restaurantEntity : restaurantEntityList) {
            RestaurantList restaurant = new RestaurantList();
            restaurant.setId(UUID.fromString(restaurantEntity.getUuid()));
            restaurant.setRestaurantName(restaurantEntity.getRestaurantName());
            restaurant.setPhotoURL(restaurantEntity.getPhotoUrl());
            restaurant.setCustomerRating(
                    new BigDecimal(Double.toString(restaurantEntity.getCustomerRating()))
                            .setScale(2, RoundingMode.HALF_DOWN));
            restaurant.setNumberCustomersRated(restaurantEntity.getNumberOfCustomersRated());

            // extract address and transform to response object
            RestaurantDetailsResponseAddress address = new RestaurantDetailsResponseAddress();
            address.setId(UUID.fromString((restaurantEntity.getAddress().getUuid())));
            address.setFlatBuildingName(restaurantEntity.getAddress().getFlatBuilNo());
            address.setLocality(restaurantEntity.getAddress().getLocality());
            address.setCity(restaurantEntity.getAddress().getCity());
            address.setPincode(restaurantEntity.getAddress().getPincode());
            RestaurantDetailsResponseAddressState state = new RestaurantDetailsResponseAddressState();
            state.setId(UUID.fromString(restaurantEntity.getAddress().getState().getUuid()));
            state.setStateName(restaurantEntity.getAddress().getState().getStateName());
            address.setState(state);
            restaurant.setAddress(address);

            // extract category and sort in alphabetical order
            List<CategoryEntity> categoryEntityList =
                    categoryService.getCategoriesByRestaurant(restaurantEntity.getUuid());
            List<String> categoryNames = new ArrayList<>();
            for (CategoryEntity category : categoryEntityList) {
                categoryNames.add(category.getCategoryName());
            }
            Collections.sort(categoryNames);
            String categoryString = String.join(", ", categoryNames);
            restaurant.setCategories(categoryString);

            restaurantList.add(restaurant);
        }
        restaurantList =
                restaurantList.stream()
                        .sorted(
                                Comparator.comparing(
                                        RestaurantList::getRestaurantName, String.CASE_INSENSITIVE_ORDER))
                        .collect(Collectors.toList());
        RestaurantListResponse restaurantListResponse = new RestaurantListResponse();
        restaurantListResponse.setRestaurants(restaurantList);
        return new ResponseEntity<>(restaurantListResponse, HttpStatus.OK);
    }

    /**
     * This method  takes category as the input which if served by a restaurant,the restaurant is included
     * in the return list of restaurants
     *
     * @return ResponseEntity with list of all of the Restaurants
     * @throws CategoryNotFoundException if the name is empty
     */
    @CrossOrigin
    @RequestMapping(path = "/{restaurant_id}", method = RequestMethod.GET)
    public ResponseEntity<RestaurantDetailsResponse> getRestaurantById(
            @PathVariable("restaurant_id") String uuid)
            throws RestaurantNotFoundException, CategoryNotFoundException {
        RestaurantDetailsResponse restaurant = new RestaurantDetailsResponse();
        // Get Restaurants information
        RestaurantEntity restaurantEntity = restaurantService.restaurantByUUID(uuid);

        // transform restaurant information into response objects
        restaurant.setId(UUID.fromString(restaurantEntity.getUuid()));
        restaurant.setRestaurantName(restaurantEntity.getRestaurantName());
        restaurant.setPhotoURL(restaurantEntity.getPhotoUrl());
        restaurant.setCustomerRating(
                new BigDecimal(Double.toString(restaurantEntity.getCustomerRating()))
                        .setScale(2, RoundingMode.HALF_DOWN));
        restaurant.setAveragePrice(restaurantEntity.getAveragePriceForTwo());
        restaurant.setNumberCustomersRated(restaurantEntity.getNumberOfCustomersRated());

        // extract address and transform to response object
        RestaurantDetailsResponseAddress address = new RestaurantDetailsResponseAddress();
        address.setId(UUID.fromString((restaurantEntity.getAddress().getUuid())));
        address.setFlatBuildingName(restaurantEntity.getAddress().getFlatBuilNo());
        address.setLocality(restaurantEntity.getAddress().getLocality());
        address.setCity(restaurantEntity.getAddress().getCity());
        address.setPincode(restaurantEntity.getAddress().getPincode());
        RestaurantDetailsResponseAddressState state = new RestaurantDetailsResponseAddressState();
        state.setId(UUID.fromString(restaurantEntity.getAddress().getState().getUuid()));
        state.setStateName(restaurantEntity.getAddress().getState().getStateName());
        address.setState(state);
        restaurant.setAddress(address);

        // extract categories
        List<CategoryEntity> categoryEntityList = categoryService.getCategoriesByRestaurant(uuid);
        List<CategoryList> categoryList = new ArrayList<>();

        // from the list of categories, categorize the restaurant items into the different categories
        for (CategoryEntity ce : categoryEntityList) {
            CategoryList category = new CategoryList();
            category.setId(UUID.fromString(ce.getUuid()));
            category.setCategoryName(ce.getCategoryName());
            List<ItemEntity> itemEntityList =
                    itemService.getItemsByCategoryAndRestaurant(uuid, ce.getUuid());
            List<ItemList> itemListList = new ArrayList<>();
            for (ItemEntity itemEntity : itemEntityList) {
                ItemList item = new ItemList();
                item.setId(UUID.fromString(itemEntity.getUuid()));
                item.setItemName(itemEntity.getItemName());
                item.setPrice(itemEntity.getPrice());
                item.setItemType(ItemList.ItemTypeEnum.fromValue(itemEntity.getType().getValue()));
                itemListList.add(item);
            }
            itemListList =
                    itemListList.stream()
                            .sorted(Comparator.comparing(ItemList::getItemName, String.CASE_INSENSITIVE_ORDER))
                            .collect(Collectors.toList());
            category.setItemList(itemListList);
            categoryList.add(category);
        }
        categoryList =
                categoryList.stream()
                        .sorted(
                                Comparator.comparing(CategoryList::getCategoryName, String.CASE_INSENSITIVE_ORDER))
                        .collect(Collectors.toList());
        restaurant.categories(categoryList);

        return new ResponseEntity<RestaurantDetailsResponse>(restaurant, HttpStatus.OK);
    }

    /**
     * This method s takes updated password information from the customer and updates it in the system
     *
     * @param authorization Customer's access token as request header param
     * @param customerRating decimal value between 1.0 and 5.0
     * @param restaurantId restaurant id
     * @return ResponseEntity with message
     * @throws AuthorizationFailedException on incorrect/invalid access token
     * @throws RestaurantNotFoundException if id is empty, No restaurant by this id
     * @throws InvalidRatingException rating not beween 1.0 to 5.0
     * @throws UnexpectedException on any other errors
     */
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.PUT,
            path = "/{restaurant_id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantUpdatedResponse> updateRestaurantDetails(
            @RequestParam(name = "customer_rating") final Double customerRating,
            @PathVariable("restaurant_id") final String restaurantId,
            @RequestHeader("authorization") final String authorization)
            throws RestaurantNotFoundException, AuthorizationFailedException, InvalidRatingException {
        final String accessToken = AppUtils.getBearerAuthToken(authorization);
        final CustomerEntity customerEntity = customerService.getCustomer(accessToken);

        // get restaurant entity
        RestaurantEntity restaurant = restaurantService.restaurantByUUID(restaurantId);

        // call to update the rating
        RestaurantEntity updatedRestaurant =
                restaurantService.updateRestaurantRating(restaurant, customerRating);
        RestaurantUpdatedResponse restaurantUpdatedResponse =
                new RestaurantUpdatedResponse()
                        .id(UUID.fromString(restaurantId))
                        .status("RESTAURANT RATING UPDATED SUCCESSFULLY");
        return new ResponseEntity<>(restaurantUpdatedResponse, HttpStatus.OK);
    }
}
