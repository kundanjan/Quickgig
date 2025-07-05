# Quickgig

Quickgig is an Android platform that connects freelancers and clients, providing a seamless space to find gigs, hire talent, and communicate instantly. The app integrates CometChat for real-time messaging (text, voice, and video) and leverages Firebase for user management and gig storage.

---

## Features

- **Role-Based Home**
  - **Clients:** Browse and hire suitable freelancers.
  - **Freelancers:** View and approach available gigs.

- **Direct Communication**
  - Tap on a freelancer or gig to start a conversation instantly.
  - In-app chat powered by CometChat.

- **Rich Communication Tools**
  - Text messaging.
  - Voice messaging.
  - Video calling.

- **User Management & Authentication**
  - Secure login and registration using CometChat authentication.
  - User and gig data stored on Firebase.

---

## Screenshots

## 📸 Additional Screenshots

| Screen 1 | Screen 2 | Screen 3 | Screen 4 |
|----------|----------|----------|----------|
| ![](https://github.com/user-attachments/assets/fdfe4cbb-eaef-4e7e-8da8-151bec6081ce) | ![](https://github.com/user-attachments/assets/d10dfb58-001e-455c-ab33-029231c1a9a5) | ![](https://github.com/user-attachments/assets/26ae8c40-0d69-4989-b07c-5c423abae64b) | ![](https://github.com/user-attachments/assets/2450b09f-fde3-4ffc-bb2a-08c92bcf5a5b) |
| Screen 5 | Screen 6 | Screen 7 |          |
| ![](https://github.com/user-attachments/assets/7ac65b66-b989-495d-b66b-478bcec85a80) | ![](https://github.com/user-attachments/assets/67395df0-28d4-47a0-b635-ce63b5849062) | ![](https://github.com/user-attachments/assets/0ebdf377-7fad-4d2e-b611-42b0d4d59edc) |          |



---

## Getting Started

### Prerequisites

- Android Studio (latest stable version recommended)
- Android device or emulator (API 21+)
- Firebase account (for Firestore/Realtime Database and Authentication)
- [CometChat Pro account](https://www.cometchat.com/) (for App ID, region, and Auth Key)

### Required Credentials

You will need the following from your CometChat dashboard:
- **App ID**
- **Region**
- **Auth Key**

---

## Setup Instructions

1. **Clone the Repository**

   ```bash
   git clone https://github.com/kundanjan/Quickgig.git
   cd Quickgig
   ```

2. **Open in Android Studio**

   - Open Android Studio.
   - Select “Open an existing project” and choose the cloned `Quickgig` folder.

3. **Configure Firebase**

   - Go to [Firebase Console](https://console.firebase.google.com/).
   - Create a new project (or use an existing one).
   - Add an Android app to your Firebase project.
   - Download the `google-services.json` file and place it in the `app/` directory.
   - Enable Firestore/Realtime Database and Authentication as needed.

4. **Configure CometChat**

   - Sign up or log in at [CometChat Dashboard](https://app.cometchat.com/).
   - Create a new app to get your **App ID**, **Region**, and **Auth Key**.
   - In your project, locate where CometChat credentials are required (usually in a config or constants file).
   - Replace placeholder values with your actual credentials.

5. **Build and Run**

   - Connect your Android device or start an emulator.
   - Click “Run” in Android Studio.

> **Note:**  
> Quickgig is an Android application and cannot run natively on PC. To use it on a PC, you need to use an Android emulator.

---

## Usage

- Register or log in as a client or freelancer.
- If you are a client, browse freelancers and initiate communication by tapping on their profile.
- If you are a freelancer, browse gigs and approach clients directly.
- Use chat, voice, or video features to communicate and collaborate.

---

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request for any feature or improvement.


---

## Support

For issues, please open an [issue on GitHub](https://github.com/kundanjan/Quickgig/issues).
