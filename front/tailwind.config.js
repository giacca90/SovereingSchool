/** @type {import('tailwindcss').Config} */
module.exports = {
	content: ['./src/**/*.{html,js,ts}'],
	theme: {
		extend: {
			keyframes: {
				slideIn: {
					'0%': { transform: 'translateX(1300%)' },
					'100%': { transform: 'translateX(-200%)' },
				},
			},
			animation: {
				slideIn: 'slideIn 40s ease-out infinite',
			},
		},
	},
	plugins: [],
};
