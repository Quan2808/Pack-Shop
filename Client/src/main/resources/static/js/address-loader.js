document.addEventListener("DOMContentLoaded", loadProvinces);

async function loadProvinces() {
  try {
    const response = await fetch("https://provinces.open-api.vn/api/p/");
    const provinces = await response.json();

    const provinceSelect = document.getElementById("province");
    provinces.forEach((province) => {
      const option = document.createElement("option");
      option.value = province.name; // Use name as value
      option.text = province.name;
      option.dataset.code = province.code; // Store code for later API use
      provinceSelect.appendChild(option);
    });
  } catch (error) {
    console.error("Error loading provinces:", error);
  }
}

async function loadDistricts() {
  const provinceSelect = document.getElementById("province");
  const districtSelect = document.getElementById("district");
  const wardSelect = document.getElementById("ward");

  // Reset and disable subsequent selects
  districtSelect.innerHTML = '<option value="">Select District</option>';
  wardSelect.innerHTML = '<option value="">Select Ward/Commune</option>';
  districtSelect.disabled = true;
  wardSelect.disabled = true;

  const selectedProvinceCode = Array.from(provinceSelect.options).find(
    (option) => option.value === provinceSelect.value
  )?.dataset.code;

  if (!selectedProvinceCode) return;

  try {
    const response = await fetch(
      `https://provinces.open-api.vn/api/p/${selectedProvinceCode}?depth=2`
    );
    const data = await response.json();
    const districts = data.districts;

    districts.forEach((district) => {
      const option = document.createElement("option");
      option.value = district.name; // Use name as value
      option.text = district.name;
      option.dataset.code = district.code; // Store code for later API use
      districtSelect.appendChild(option);
    });

    districtSelect.disabled = false;
  } catch (error) {
    console.error("Error loading districts:", error);
  }
}

async function loadWards() {
  const districtSelect = document.getElementById("district");
  const wardSelect = document.getElementById("ward");

  // Reset and disable ward select
  wardSelect.innerHTML = '<option value="">Select Ward/Commune</option>';
  wardSelect.disabled = true;

  const selectedDistrictCode = Array.from(districtSelect.options).find(
    (option) => option.value === districtSelect.value
  )?.dataset.code;

  if (!selectedDistrictCode) return;

  try {
    const response = await fetch(
      `https://provinces.open-api.vn/api/d/${selectedDistrictCode}?depth=2`
    );
    const data = await response.json();
    const wards = data.wards;

    wards.forEach((ward) => {
      const option = document.createElement("option");
      option.value = ward.name; // Use name as value
      option.text = ward.name;
      wardSelect.appendChild(option);
    });

    wardSelect.disabled = false;
  } catch (error) {
    console.error("Error loading wards:", error);
  }
}
