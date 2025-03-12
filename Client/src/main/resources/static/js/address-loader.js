// Load provinces for new address form
async function loadProvinces() {
  try {
    const response = await fetch("https://provinces.open-api.vn/api/p/");
    const provinces = await response.json();

    const provinceSelect = document.getElementById("province");
    if (provinceSelect) {
      provinces.forEach((province) => {
        const option = document.createElement("option");
        option.value = province.name;
        option.text = province.name;
        option.dataset.code = province.code;
        provinceSelect.appendChild(option);
      });
    }
  } catch (error) {
    console.error("Error loading provinces:", error);
  }
}

// Function to show edit modal and load address data
window.showEditModal = function (button) {
  // Get the address ID from the button's data attribute
  const addressId = button.getAttribute("data-id");

  // Find the address item in the DOM
  const addressItem = button.closest(".accordion-item");

  if (!addressItem) {
    console.error("Could not find parent accordion item");
    return;
  }

  // Extract data from the address item
  const aliasNameElement = addressItem.querySelector(
    ".accordion-button .fw-bold"
  );
  const fullAddressElement = addressItem.querySelector(
    ".accordion-button .text-muted"
  );

  if (!aliasNameElement || !fullAddressElement) {
    console.error("Could not find required elements in the accordion");
    return;
  }

  const aliasName = aliasNameElement.textContent.trim();
  const fullAddress = fullAddressElement.textContent.trim();

  // Get detailed address data from the accordion body
  const accordionBody = addressItem.querySelector(".accordion-body");

  if (!accordionBody) {
    console.error("Could not find accordion body");
    return;
  }

  // Get the streetAddress, ward, district, province elements
  const streetAddressElement = accordionBody.querySelector(
    "div:nth-child(1) .text-muted"
  );
  const wardElement = accordionBody.querySelector(
    "div:nth-child(2) .text-muted"
  );
  const districtElement = accordionBody.querySelector(
    "div:nth-child(3) .text-muted"
  );
  const provinceElement = accordionBody.querySelector(
    "div:nth-child(4) .text-muted"
  );

  if (
    !streetAddressElement ||
    !wardElement ||
    !districtElement ||
    !provinceElement
  ) {
    console.error("Could not find address detail elements");
    return;
  }

  const streetAddress = streetAddressElement.textContent.trim();
  const ward = wardElement.textContent.trim();
  const district = districtElement.textContent.trim();
  const province = provinceElement.textContent.trim();

  // Check if it's the default address
  const isDefault = addressItem.querySelector(".badge-info") !== null;

  // Get the hidden ID field
  const hiddenIdField = document.querySelector(
    '#updateAddressModal input[name="id"]'
  );
  if (hiddenIdField) {
    hiddenIdField.value = addressId;
  }

  // Populate form fields
  const aliasNameField = document.getElementById("editAliasName");
  const streetAddressField = document.getElementById("editStreetAddress");
  const isDefaultField = document.getElementById("editIsDefault");

  if (aliasNameField) aliasNameField.value = aliasName;
  if (streetAddressField) streetAddressField.value = streetAddress;
  if (isDefaultField) isDefaultField.checked = isDefault;

  // Update URL in the form action
  const form = document.querySelector("#updateAddressModal form");
  if (form) {
    form.action = `/account/profile/update-address/${addressId}`;
  }

  // Load and select province/district/ward
  loadEditProvinces(province, district, ward);

  // Activate labels for MDB input fields
  if (typeof mdb !== "undefined" && mdb.Input) {
    document
      .querySelectorAll("#updateAddressModal .form-outline")
      .forEach((input) => {
        const instance = mdb.Input.getInstance(input);
        if (instance) {
          instance.update();
        }
      });
  }
};

async function loadEditProvinces(
  selectedProvince,
  selectedDistrict,
  selectedWard
) {
  try {
    const response = await fetch("https://provinces.open-api.vn/api/p/");
    const provinces = await response.json();

    const provinceSelect = document.getElementById("editProvince");
    if (!provinceSelect) {
      console.error("Province select element not found");
      return;
    }

    // Clear previous options except the first one
    provinceSelect.innerHTML = '<option value="">Select Province/City</option>';

    provinces.forEach((province) => {
      const option = document.createElement("option");
      option.value = province.name;
      option.text = province.name;
      option.dataset.code = province.code;
      option.selected = province.name === selectedProvince;
      provinceSelect.appendChild(option);
    });

    // If we have a province selected, load its districts
    if (selectedProvince) {
      await loadEditDistricts(selectedProvince, selectedDistrict, selectedWard);
    }

    // Refresh MDB select if initialized
    if (typeof mdb !== "undefined" && mdb.Select) {
      const selectInstance = mdb.Select.getInstance(provinceSelect);
      if (selectInstance) {
        selectInstance.dispose();
      }
      new mdb.Select(provinceSelect);
    }
  } catch (error) {
    console.error("Error loading provinces:", error);
  }
}

async function loadEditDistricts(provinceName, selectedDistrict, selectedWard) {
  const provinceSelect = document.getElementById("editProvince");
  const districtSelect = document.getElementById("editDistrict");

  if (!provinceSelect || !districtSelect) {
    console.error("Province or district select element not found");
    return;
  }

  // Find the province code based on name
  const selectedOption = Array.from(provinceSelect.options).find(
    (option) => option.value === provinceName
  );

  if (!selectedOption || !selectedOption.dataset.code) {
    console.error("Could not find province code for:", provinceName);
    return;
  }

  const selectedProvinceCode = selectedOption.dataset.code;

  try {
    const response = await fetch(
      `https://provinces.open-api.vn/api/p/${selectedProvinceCode}?depth=2`
    );
    const data = await response.json();
    const districts = data.districts;

    // Clear previous options except the first one
    districtSelect.innerHTML = '<option value="">Select District</option>';

    districts.forEach((district) => {
      const option = document.createElement("option");
      option.value = district.name;
      option.text = district.name;
      option.dataset.code = district.code;
      option.selected = district.name === selectedDistrict;
      districtSelect.appendChild(option);
    });

    // Enable the district select
    districtSelect.disabled = false;

    // If we have a district selected, load its wards
    if (selectedDistrict) {
      await loadEditWards(selectedDistrict, selectedWard);
    }

    // Refresh MDB select if initialized
    if (typeof mdb !== "undefined" && mdb.Select) {
      const selectInstance = mdb.Select.getInstance(districtSelect);
      if (selectInstance) {
        selectInstance.dispose();
      }
      new mdb.Select(districtSelect);
    }
  } catch (error) {
    console.error("Error loading districts:", error);
  }
}

async function loadEditWards(districtName, selectedWard) {
  const districtSelect = document.getElementById("editDistrict");
  const wardSelect = document.getElementById("editWard");

  if (!districtSelect || !wardSelect) {
    console.error("District or ward select element not found");
    return;
  }

  // Find the district code based on name
  const selectedOption = Array.from(districtSelect.options).find(
    (option) => option.value === districtName
  );

  if (!selectedOption || !selectedOption.dataset.code) {
    console.error("Could not find district code for:", districtName);
    return;
  }

  const selectedDistrictCode = selectedOption.dataset.code;

  try {
    const response = await fetch(
      `https://provinces.open-api.vn/api/d/${selectedDistrictCode}?depth=2`
    );
    const data = await response.json();
    const wards = data.wards;

    // Clear previous options except the first one
    wardSelect.innerHTML = '<option value="">Select Ward/Commune</option>';

    wards.forEach((ward) => {
      const option = document.createElement("option");
      option.value = ward.name;
      option.text = ward.name;
      option.selected = ward.name === selectedWard;
      wardSelect.appendChild(option);
    });

    // Enable the ward select
    wardSelect.disabled = false;

    // Refresh MDB select if initialized
    if (typeof mdb !== "undefined" && mdb.Select) {
      const selectInstance = mdb.Select.getInstance(wardSelect);
      if (selectInstance) {
        selectInstance.dispose();
      }
      new mdb.Select(wardSelect);
    }
  } catch (error) {
    console.error("Error loading wards:", error);
  }
}

// Load provinces on page load for both new and edit forms
document.addEventListener("DOMContentLoaded", function () {
  // Load provinces for the new address form
  loadProvinces();

  // Initialize MDB components if needed
  if (typeof mdb !== "undefined" && mdb.Select && mdb.Select.init) {
    document.querySelectorAll(".select").forEach((select) => {
      new mdb.Select(select);
    });
  }
});

// Update the event handlers for the edit form
function loadDistricts(formType) {
  if (formType === "edit") {
    const provinceSelect = document.getElementById("editProvince");
    if (provinceSelect) {
      const selectedProvince = provinceSelect.value;
      loadEditDistricts(selectedProvince);
    }
  } else {
    // Handle regular form as previously defined
    // This is for the original loadDistricts function
    const provinceSelect = document.getElementById("province");
    const districtSelect = document.getElementById("district");
    const wardSelect = document.getElementById("ward");

    if (!provinceSelect || !districtSelect || !wardSelect) return;

    // Reset and disable subsequent selects
    districtSelect.innerHTML = '<option value="">Select District</option>';
    wardSelect.innerHTML = '<option value="">Select Ward/Commune</option>';
    districtSelect.disabled = true;
    wardSelect.disabled = true;

    const selectedOption = Array.from(provinceSelect.options).find(
      (option) => option.value === provinceSelect.value
    );

    if (!selectedOption || !selectedOption.dataset.code) return;

    const selectedProvinceCode = selectedOption.dataset.code;

    fetch(`https://provinces.open-api.vn/api/p/${selectedProvinceCode}?depth=2`)
      .then((response) => response.json())
      .then((data) => {
        const districts = data.districts;

        districts.forEach((district) => {
          const option = document.createElement("option");
          option.value = district.name;
          option.text = district.name;
          option.dataset.code = district.code;
          districtSelect.appendChild(option);
        });

        districtSelect.disabled = false;

        // Refresh MDB select if initialized
        if (typeof mdb !== "undefined" && mdb.Select) {
          const selectInstance = mdb.Select.getInstance(districtSelect);
          if (selectInstance) {
            selectInstance.dispose();
          }
          new mdb.Select(districtSelect);
        }
      })
      .catch((error) => {
        console.error("Error loading districts:", error);
      });
  }
}

function loadWards(formType) {
  if (formType === "edit") {
    const districtSelect = document.getElementById("editDistrict");
    if (districtSelect) {
      const selectedDistrict = districtSelect.value;
      loadEditWards(selectedDistrict);
    }
  } else {
    // Handle regular form as previously defined
    const districtSelect = document.getElementById("district");
    const wardSelect = document.getElementById("ward");

    if (!districtSelect || !wardSelect) return;

    // Reset and disable ward select
    wardSelect.innerHTML = '<option value="">Select Ward/Commune</option>';
    wardSelect.disabled = true;

    const selectedOption = Array.from(districtSelect.options).find(
      (option) => option.value === districtSelect.value
    );

    if (!selectedOption || !selectedOption.dataset.code) return;

    const selectedDistrictCode = selectedOption.dataset.code;

    fetch(`https://provinces.open-api.vn/api/d/${selectedDistrictCode}?depth=2`)
      .then((response) => response.json())
      .then((data) => {
        const wards = data.wards;

        wards.forEach((ward) => {
          const option = document.createElement("option");
          option.value = ward.name;
          option.text = ward.name;
          wardSelect.appendChild(option);
        });

        wardSelect.disabled = false;

        // Refresh MDB select if initialized
        if (typeof mdb !== "undefined" && mdb.Select) {
          const selectInstance = mdb.Select.getInstance(wardSelect);
          if (selectInstance) {
            selectInstance.dispose();
          }
          new mdb.Select(wardSelect);
        }
      })
      .catch((error) => {
        console.error("Error loading wards:", error);
      });
  }
}
