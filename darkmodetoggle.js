const btn = document.getElementById("toggle");
const theme = document.querySelector("#theme-link");
btn.addEventListener("click", function() {
  if (theme.getAttribute("href") == "resources/css/style.css") {
    theme.href = "resources/css/dark-theme.css";
  } else {
    theme.href = "resources/css/style.css";
  }
});