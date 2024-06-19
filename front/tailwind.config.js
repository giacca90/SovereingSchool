/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,js}"],
  theme: {
    extend: {
      keyframes: {
        slideIn: {
          "0%": { transform: "translateX(1300%)" },
          "100%": { transform: "translateX(-200%)" },
        },
      },
      animation: {
        slideIn: "slideIn 10s ease-out infinite",
      },
    },
  },
  plugins: [],
};
