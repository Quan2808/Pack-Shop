// Constants
const API_BASE = "https://provinces.open-api.vn/api/";
let provincesData = [];
let districtsData = [];
let wardsData = [];

// DOM Elements
const provinceSelect = document.getElementById("provinceSelect");
const districtSelect = document.getElementById("districtSelect");
const wardSelect = document.getElementById("wardSelect");
const searchInput = document.getElementById("searchInput");

// Initialize tooltips and select
document.addEventListener("DOMContentLoaded", function () {
  // Initialize MDB components
  const selects = document.querySelectorAll(".select");
  selects.forEach((select) => {
    new mdb.Select(select);
  });

  // Initialize textfields
  const inputs = document.querySelectorAll(".form-outline");
  inputs.forEach((input) => {
    new mdb.Input(input).init();
  });

  // Load initial data
  loadProvinces();
});

// Load provinces data
async function loadProvinces() {
  try {
    provinceSelect.disabled = true;
    document.querySelector(".loading-spinner")?.classList.remove("d-none");

    const response = await fetch(`${API_BASE}?depth=1`);
    provincesData = await response.json();

    populateSelect(provinceSelect, provincesData);
    provinceSelect.disabled = false;

    const provinceSelectInstance = mdb.Select.getInstance(provinceSelect);
    if (provinceSelectInstance) {
      provinceSelectInstance.dispose();
    }
    new mdb.Select(provinceSelect).init();

    document.querySelector(".loading-spinner")?.classList.add("d-none");
  } catch (error) {
    console.error("Error loading provinces:", error);
    showErrorToast("Unable to load province/city data");
  }
}

// Load districts for a specific province
async function loadDistricts(provinceCode) {
  try {
    districtSelect.disabled = true;
    document.querySelector(".loading-spinner")?.classList.remove("d-none");

    const response = await fetch(`${API_BASE}p/${provinceCode}?depth=2`);
    const data = await response.json();
    districtsData = data.districts;

    populateSelect(districtSelect, districtsData);
    districtSelect.disabled = false;

    wardSelect.disabled = true;
    wardSelect.innerHTML =
      '<option value="" selected>Select Ward/Commune</option>';

    const districtSelectInstance = mdb.Select.getInstance(districtSelect);
    if (districtSelectInstance) {
      districtSelectInstance.dispose();
    }
    new mdb.Select(districtSelect).init();

    const wardSelectInstance = mdb.Select.getInstance(wardSelect);
    if (wardSelectInstance) {
      wardSelectInstance.dispose();
    }
    new mdb.Select(wardSelect).init();

    document.querySelector(".loading-spinner")?.classList.add("d-none");
  } catch (error) {
    console.error("Error loading districts:", error);
    showErrorToast("Unable to load district data");
  }
}

// Load wards for a specific district
async function loadWards(districtCode) {
  try {
    wardSelect.disabled = true;
    document.querySelector(".loading-spinner")?.classList.remove("d-none");

    const response = await fetch(`${API_BASE}d/${districtCode}?depth=2`);
    const data = await response.json();
    wardsData = data.wards;

    populateSelect(wardSelect, wardsData);
    wardSelect.disabled = false;

    const wardSelectInstance = mdb.Select.getInstance(wardSelect);
    if (wardSelectInstance) {
      wardSelectInstance.dispose();
    }
    new mdb.Select(wardSelect).init();

    document.querySelector(".loading-spinner")?.classList.add("d-none");
  } catch (error) {
    console.error("Error loading wards:", error);
    showErrorToast("Unable to load ward/commune data");
  }
}

// Populate select with data, using code as value instead of name
function populateSelect(selectElement, data) {
  selectElement.innerHTML = `<option value="" selected>Select...</option>`;

  data.forEach((item) => {
    const option = document.createElement("option");
    option.value = item.code; // Use code as value
    option.textContent = item.name; // Display name as text
    selectElement.appendChild(option);
  });
}

// Show error toast notification
function showErrorToast(message) {
  const toastContainer =
    document.querySelector(".toast-container") || createToastContainer();

  const toastElement = document.createElement("div");
  toastElement.className = "toast fade show";
  toastElement.innerHTML = `
    <div class="toast-header bg-danger text-white">
      <strong class="me-auto">Error</strong>
      <button type="button" class="btn-close btn-close-white" data-mdb-dismiss="toast"></button>
    </div>
    <div class="toast-body">
      ${message}
    </div>
  `;

  toastContainer.appendChild(toastElement);
  const toast = new mdb.Toast(toastElement);
  toast.show();

  setTimeout(() => {
    toast.hide();
    setTimeout(() => toastElement.remove(), 500);
  }, 3000);
}

// Create toast container if not exists
function createToastContainer() {
  const toastContainer = document.createElement("div");
  toastContainer.className =
    "toast-container position-fixed bottom-0 end-0 p-3";
  document.body.appendChild(toastContainer);
  return toastContainer;
}

// Event Listeners
provinceSelect.addEventListener("change", (e) => {
  const provinceCode = e.target.value;
  if (provinceCode) {
    loadDistricts(provinceCode); // Use code directly to fetch districts
  } else {
    districtSelect.disabled = true;
    wardSelect.disabled = true;

    districtSelect.innerHTML =
      '<option value="" selected>Select District</option>';
    wardSelect.innerHTML =
      '<option value="" selected>Select Ward/Commune</option>';

    const districtSelectInstance = mdb.Select.getInstance(districtSelect);
    if (districtSelectInstance) {
      districtSelectInstance.dispose();
    }
    new mdb.Select(districtSelect).init();

    const wardSelectInstance = mdb.Select.getInstance(wardSelect);
    if (wardSelectInstance) {
      wardSelectInstance.dispose();
    }
    new mdb.Select(wardSelect).init();
  }
});

districtSelect.addEventListener("change", (e) => {
  const districtCode = e.target.value;
  if (districtCode) {
    loadWards(districtCode); // Use code directly to fetch wards
  } else {
    wardSelect.disabled = true;

    wardSelect.innerHTML =
      '<option value="" selected>Select Ward/Commune</option>';

    const wardSelectInstance = mdb.Select.getInstance(wardSelect);
    if (wardSelectInstance) {
      wardSelectInstance.dispose();
    }
    new mdb.Select(wardSelect).init();
  }
});

// Search functionality with debounce
let searchTimeout;
searchInput.addEventListener("input", (e) => {
  clearTimeout(searchTimeout);

  searchTimeout = setTimeout(() => {
    const searchTerm = e.target.value.toLowerCase().trim();
    if (searchTerm.length < 2) return;

    const filteredProvinces = provincesData.filter((p) =>
      p.name.toLowerCase().includes(searchTerm)
    );

    if (filteredProvinces.length > 0) {
      populateSelect(provinceSelect, filteredProvinces);

      districtSelect.disabled = true;
      wardSelect.disabled = true;
      districtSelect.innerHTML =
        '<option value="" selected>Select District</option>';
      wardSelect.innerHTML =
        '<option value="" selected>Select Ward/Commune</option>';

      const provinceSelectInstance = mdb.Select.getInstance(provinceSelect);
      if (provinceSelectInstance) {
        provinceSelectInstance.dispose();
      }
      new mdb.Select(provinceSelect).init();

      const districtSelectInstance = mdb.Select.getInstance(districtSelect);
      if (districtSelectInstance) {
        districtSelectInstance.dispose();
      }
      new mdb.Select(districtSelect).init();

      const wardSelectInstance = mdb.Select.getInstance(wardSelect);
      if (wardSelectInstance) {
        wardSelectInstance.dispose();
      }
      new mdb.Select(wardSelect).init();
    }
  }, 300); // 300ms debounce delay
});

// Initialize
loadProvinces();
