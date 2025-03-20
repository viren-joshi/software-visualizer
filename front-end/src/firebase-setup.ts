import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
const firebaseConfig = {
  apiKey: "",
  authDomain: "software-vizualizer.firebaseapp.com",
  projectId: "software-vizualizer",
  storageBucket: "software-vizualizer.firebasestorage.app",
  messagingSenderId: "364944267344",
  appId: "1:364944267344:web:7b120929c7c1a3f0d49955",
  measurementId: "G-ZHXT34T4CN"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
