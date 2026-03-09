import { useEffect } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";

function OAuthSuccess() {

  const [params] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {

    const userId = params.get("userId");

    if(userId){
      localStorage.setItem("gmailUserId", userId);
    }

  window.location.href = "/";


  }, [params]);

  return <div>Connecting Gmail...</div>;
}

export default OAuthSuccess;