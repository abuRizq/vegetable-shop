import { User } from "../model/type";

async function validateTokenAndGetUser(): Promise<User | null> {
  try {
    const response = await fetch(`http://localhost:8080/api/users/me`, {
      method: "GET",
      credentials: "include",
    });
    if (!response.ok) {
      return null;
    }
    const data = await response.json();
    const User = data.data;
    console.log("form the vrfiy fun :" + data.token);
    return User;
  } catch (error) {
    console.error(error);
    throw error;
  }
}
export { validateTokenAndGetUser };
