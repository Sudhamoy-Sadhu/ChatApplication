import { createContext, useState } from "react";

export const RequestCountContext = createContext(null);

export function RequestCountProvider({ children }) {
  const [requestCount, setRequestCount] = useState(0);

  return (
    <RequestCountContext.Provider value={{ requestCount, setRequestCount }}>
      {children}
    </RequestCountContext.Provider>
  );
}
