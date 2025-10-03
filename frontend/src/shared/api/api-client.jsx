/***
 * centralized api requests using fetch || axios for the
 * if the error stats is 401 , 403 , 404 , 500 ,
 * axios VS fetch
 * 1- no response .JSON in axios
 * 2- error => directly throw in the catch object
 * 3- 401=> redirect login page
 * 4- easier for using ,
 *
 */

 const tempRes = (res) => {
  const status = res.status;
  const message = res.data.message;
  return {
    status,
    message,
  };
};



export function cnterlizedHandlerError(codeStutes) {
  switch (codeStutes) {
    case 400:
      
      // bad use from user
      break;
    case 401:
      // need to login again
      break;
    case 403:
      // unAuthorization
      break;
    case 500:
      // internal server Error
      break;
    case 501:

      break;
    default:
      break;
  }
}
