import { createContext, useContext, useState } from "react";

const PageManagerContext = createContext();

export const usePageManager = () => useContext(PageManagerContext);

export default function PageManagerProvider({ children }) {
  const [activePage, setActivePage] = useState("home");  

  const goToPage = (pageName) => setActivePage(pageName);
  const goBack = () => setActivePage("home");

  return (
    <PageManagerContext.Provider value={{ activePage, goToPage, goBack }}>
      {children}
    </PageManagerContext.Provider>
  );
}
