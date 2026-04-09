(function () {
  const TOAST_DURATION = 1500;
  const CYCLE_KEY = "product-ui-cycle";

  const pageMap = {
    home: "官网首页",
    features: "功能价值",
    pricing: "订阅定价",
    "auth-login": "登录",
    "auth-register": "注册",
    onboarding: "新手引导",
    workspace: "创作工作台",
    "result-delivery": "结果交付",
    history: "历史记录",
    checkout: "支付页",
    "payment-success": "支付成功",
    subscription: "订阅中心",
    profile: "个人中心",
    referral: "邀请返利",
    help: "帮助中心",
    "not-found": "404 页面"
  };

  let timer = null;

  function showToast(message) {
    const toast = document.querySelector(".toast");
    if (!toast) return;
    toast.textContent = message;
    toast.classList.add("show");
    if (timer) window.clearTimeout(timer);
    timer = window.setTimeout(() => {
      toast.classList.remove("show");
    }, TOAST_DURATION);
  }

  function setNav(page) {
    document.querySelectorAll("[data-page]").forEach((node) => {
      if (node.getAttribute("data-page") === page) {
        node.setAttribute("aria-current", "page");
      } else {
        node.removeAttribute("aria-current");
      }
    });
  }

  function setCrumb(page) {
    const crumb = document.querySelector("[data-crumb]");
    if (!crumb) return;
    crumb.textContent = pageMap[page] || "页面";
  }

  function getCycle() {
    try {
      const value = window.localStorage.getItem(CYCLE_KEY);
      if (value === "monthly" || value === "yearly") return value;
    } catch (_error) {
      return "monthly";
    }
    return "monthly";
  }

  function setCycle(cycle) {
    document.body.setAttribute("data-cycle", cycle);
    document.querySelectorAll("[data-price-monthly]").forEach((node) => {
      const monthly = node.getAttribute("data-price-monthly");
      const yearly = node.getAttribute("data-price-yearly");
      node.textContent = cycle === "yearly" ? yearly || "" : monthly || "";
    });

    document.querySelectorAll(".cycle-switch").forEach((group) => {
      group.querySelectorAll("[data-cycle]").forEach((btn) => {
        const active = btn.getAttribute("data-cycle") === cycle;
        btn.classList.toggle("active", active);
      });
    });
  }

  function bindCycle() {
    document.querySelectorAll(".cycle-switch [data-cycle]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const cycle = btn.getAttribute("data-cycle");
        if (cycle !== "monthly" && cycle !== "yearly") return;
        setCycle(cycle);
        try {
          window.localStorage.setItem(CYCLE_KEY, cycle);
        } catch (_error) {}
        showToast(cycle === "yearly" ? "已切换为年付视图" : "已切换为月付视图");
      });
    });
  }

  function bindActions() {
    document.querySelectorAll(".js-prototype-action").forEach((btn) => {
      btn.addEventListener("click", () => {
        const message = btn.getAttribute("data-message") || "已触发原型操作";
        showToast(message);
      });
    });
  }

  function init() {
    const page = document.body.getAttribute("data-page") || "home";
    setNav(page);
    setCrumb(page);
    bindActions();
    bindCycle();
    setCycle(getCycle());
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();