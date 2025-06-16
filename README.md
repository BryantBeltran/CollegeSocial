# College Planner App

## Project Overview

The College Planner App is an Android application designed to help students manage their college events. It features a secure login system with session management, displays a list of upcoming events fetched from a mock API, and allows users to add new events. The UI is built with a mix of traditional Android Views and modern Jetpack Compose.

## Features

* **User Authentication:**
    * Login page (`LoginActivity`) to authenticate users against a mock API.
    * Session management using `SharedPreferences` to keep users logged in across app launches.
    * "Logout" functionality to clear the session and return to the login screen.
* **Event Management:**
    * Displays a list of college events in the `MainActivity` using a `RecyclerView`.
    * Fetches event data from a RESTful API.
    * Allows users to add new events through a dedicated `AddInfoActivity` (implemented with Jetpack Compose).
    * Refreshes the event list automatically after adding a new event.
* **API Integration:**
    * Utilizes Retrofit for making network requests to a mock backend API.
    * Uses Gson for JSON serialization and deserialization.
* **Modern UI/UX:**
    * Custom `Toolbar` implementation in `MainActivity` for a clean header with app title and logout button.
    * Basic UI for login and event display.
    * `AddInfoActivity` built with Jetpack Compose for a modern declarative UI.
    * `fitsSystemWindows` handled for proper content display below system bars.

## Technologies Used

* **Language:** Kotlin (for `MainActivity`, `ApiService`, `Event`, `User` data classes, `RetrofitInstance`, `AddInfoActivity`) and Java (for `LoginActivity`).
* **IDE:** Android Studio
* **Networking:** Retrofit
* **JSON Parsing:** Gson
* **UI Toolkit:**
    * Android Views (`RelativeLayout`, `RecyclerView`, `EditText`, `Button`, `TextView`, `Toolbar`)
    * Jetpack Compose (for `AddInfoActivity`)
* **Backend Simulation:** MockAPI.io (Free Mock API Service)

## Setup and Installation

Follow these steps to get the College Planner App running on your local machine:

1.  **Clone the Repository (if applicable):**
    ```bash
    git clone <your-repo-url>
    cd CollegePlannerApp
    ```
    *(If not using Git, ensure you have all project files downloaded)*

2.  **Open in Android Studio:**
    * Launch Android Studio.
    * Select `Open an existing Android Studio project` and navigate to the project root directory.

3.  **Configure MockAPI.io Backend:**
    * Go to [mockapi.io](https://mockapi.io/) and create an account.
    * **Create a new Project.**
    * **Create two Resources:**
        * **`users` Resource:**
            * **Name:** `users` (all lowercase)
            * **Schema:** Define fields to match your `User.kt` data class (e.g., `id` (Object ID), `username` (String), `password` (String), `email` (String), `fullName` (String), `age` (Number), `location` (String), `role` (String), `phone` (String), `accountCreated` (String), `profilePicture` (String)).
            * **Data:** Go to the `Data` tab and manually add/edit records. Ensure you have specific `username` and `password` pairs (e.g., `username: "testuser"`, `password: "testpass"`) that you can use for testing login.
        * **`events` Resource:**
            * **Name:** `events` (all lowercase)
            * **Schema:** Define fields to match your `Event.kt` data class (e.g., `id` (Object ID), `event_name` (String), `date` (String), `time` (String), `location` (String)).
            * **Data:** Go to the `Data` tab and manually add/edit some event records.
    * **Copy your Base URL:** From your MockAPI.io project dashboard, copy the exact Base URL (e.g., `https://<YOUR_UNIQUE_ID>.mockapi.io/`).

4.  **Update API Configuration in Android Project:**
    * Open `app/src/main/java/com/example/newcollegeplanner/ApiService.kt` and ensure the `@GET` and `@POST` paths are correct:
        ```kotlin
        interface ApiService {
            @GET("users") fun getUsers(): Call<List<User>>
            @GET("events") fun getEvents(): Call<List<Event>>
            @POST("events") fun addEvent(@Body event: Event): Call<Void>
        }
        ```
    * Open `app/src/main/java/com/example/newcollegeplanner/LoginActivity.java` and update the `baseUrl` in `Retrofit.Builder`:
        ```java
        // ...
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("YOUR_MOCKAPI.IO_BASE_URL_HERE") // PASTE YOUR BASE URL HERE
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        // ...
        ```
    * Open `app/src/main/java/com/example/newcollegeplanner/MainActivity.kt` and update the `baseUrl` in `Retrofit.Builder`:
        ```kotlin
        // ...
        val retrofit = Retrofit.Builder()
            .baseUrl("YOUR_MOCKAPI.IO_BASE_URL_HERE") // PASTE YOUR BASE URL HERE
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        // ...
        ```
    * **(Optional: Centralized RetrofitInstance)** If you plan to use `RetrofitInstance` for all API calls, update its `baseUrl` and `api` type accordingly. Then replace local `Retrofit.Builder` calls with `RetrofitInstance.api`.

5.  **Build and Run:**
    * In Android Studio, go to `Build` > `Clean Project`.
    * Go to `Build` > `Rebuild Project`.
    * Connect an Android device or start an emulator.
    * Click the `Run` button (green triangle) in the toolbar.

## How to Use the App

1.  **Login:** Upon launching, you will be presented with the login screen. Use the `username` and `password` you set up in your MockAPI.io `users` data.
2.  **Main Screen:** After successful login, you'll see a list of events fetched from your MockAPI.io `events` resource.
3.  **Add Event:** Click the "ADD NEW EVENT" button at the bottom to open the event creation screen. Fill in the details and submit. The event list will refresh.
4.  **Logout:** Click the "LOGOUT" button in the top right of the main screen to return to the login page.
5.  **Session Persistence:** Close the app completely after logging in. Re-open it, and you should bypass the login screen and go straight to the main event list.

## Future Enhancements (Ideas)

* **User Registration:** Add a "Sign Up" option on the login page.
* **Event Details/Editing/Deletion:** Implement screens to view full event details, and allow users to edit or delete events.
* **User Profile Screen:** Display the full user profile data fetched from the API.
* **Input Validation:** More robust validation for event fields and login credentials.
* **Error Handling:** More sophisticated handling of network errors and API response failures.
* **Search/Filter:** Add functionality to search or filter events.
* **UI Refinements:** Further polish the UI/UX using more Jetpack Compose for consistency.

---
