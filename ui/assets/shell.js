(function () {
  const TECH_MODE_KEY = "ui-tech-mode";
  let toastTimer = null;
  const pageMap = {
    home: "原型总览",
    "auth-register": "注册引导",
    "auth-login": "登录与会话",
    dashboard: "控制台总览",
    tenant: "Workspace 租户",
    rbac: "角色权限",
    billing: "订阅计费",
    apikey: "API Key 管理",
    "ai-gateway": "AI Gateway",
    usage: "用量与运营"
  };

  function showToast(message) {
    const toast = document.querySelector(".toast");
    if (!toast) {
      return;
    }

    toast.textContent = message;
    toast.classList.add("show");
    if (toastTimer) {
      window.clearTimeout(toastTimer);
    }
    toastTimer = window.setTimeout(() => {
      toast.classList.remove("show");
    }, 1400);
  }

  function setCurrentNav(pageKey) {
    const links = document.querySelectorAll("[data-page]");
    links.forEach((link) => {
      if (link.getAttribute("data-page") === pageKey) {
        link.setAttribute("aria-current", "page");
      } else {
        link.removeAttribute("aria-current");
      }
    });
  }

  function setBreadcrumb(pageKey) {
    const crumb = document.querySelector("[data-crumb]");
    if (!crumb) {
      return;
    }
    const label = pageMap[pageKey] || "页面";
    crumb.textContent = label;
  }

  function getStoredTechMode() {
    try {
      const mode = window.localStorage.getItem(TECH_MODE_KEY);
      if (mode === "high" || mode === "medium") {
        return mode;
      }
    } catch (_error) {
      return "high";
    }
    return "high";
  }

  function saveTechMode(mode) {
    try {
      window.localStorage.setItem(TECH_MODE_KEY, mode);
    } catch (_error) {
      // Ignore storage write failures.
    }
  }

  function applyTechMode(mode) {
    const body = document.body;
    if (!body) {
      return;
    }
    body.classList.remove("tech-high", "tech-medium");
    body.classList.add(mode === "medium" ? "tech-medium" : "tech-high");
  }

  function getCurrentTechMode() {
    return document.body.classList.contains("tech-medium") ? "medium" : "high";
  }

  function updateToggleLabel(button, mode) {
    button.textContent = mode === "medium" ? "档位：中科技" : "档位：强科技";
    button.setAttribute("aria-label", "切换科技视觉档位");
  }

  function mountTechModeToggle() {
    const topActions = document.querySelector(".top-actions");
    if (!topActions) {
      return;
    }

    const modeButton = document.createElement("button");
    modeButton.type = "button";
    modeButton.className = "button ghost";

    const initialMode = getCurrentTechMode();
    updateToggleLabel(modeButton, initialMode);

    modeButton.addEventListener("click", () => {
      const currentMode = getCurrentTechMode();
      const nextMode = currentMode === "high" ? "medium" : "high";
      applyTechMode(nextMode);
      saveTechMode(nextMode);
      updateToggleLabel(modeButton, nextMode);
      showToast(nextMode === "high" ? "已切换到强科技模式" : "已切换到中科技模式");
    });

    topActions.insertBefore(modeButton, topActions.firstChild);
  }

  function bindPrototypeActions() {
    const buttons = document.querySelectorAll(".js-prototype-action");

    buttons.forEach((button) => {
      button.addEventListener("click", () => {
        const message = button.getAttribute("data-message") || "已触发原型交互";
        showToast(message);
      });
    });
  }

  function init() {
    const body = document.body;
    if (!body) {
      return;
    }

    const pageKey = body.getAttribute("data-page") || "home";
    applyTechMode(getStoredTechMode());
    setCurrentNav(pageKey);
    setBreadcrumb(pageKey);
    mountTechModeToggle();
    bindPrototypeActions();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
