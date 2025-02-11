const sidenav = document.getElementById("sidenav");
const instance = mdb.Sidenav.getInstance(sidenav);

let innerWidth = null;

const setMode = (e) => {
  // Check necessary for Android devices
  if (window.innerWidth === innerWidth) {
    return;
  }

  innerWidth = window.innerWidth;

  if (window.innerWidth < 992) {
    instance.changeMode("over");
    instance.hide();
  } else {
    instance.changeMode("side");
    instance.show();
  }
};

setMode();

// Event listeners
window.addEventListener("resize", setMode);
