/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js}'],
  theme: {
    extend: {
      colors: {
        ink: '#1d1d1f',
        paper: '#f5f5f7',
        copper: '#0071e3',
        moss: '#30d158',
        mute: '#86868b',
        line: '#d2d2d7'
      },
      fontFamily: {
        sans: [
          'Inter',
          '-apple-system',
          'BlinkMacSystemFont',
          '"PingFang SC"',
          '"Hiragino Sans GB"',
          '"Microsoft YaHei"',
          'system-ui',
          'sans-serif'
        ],
        serif: [
          'Inter',
          '-apple-system',
          'BlinkMacSystemFont',
          '"PingFang SC"',
          '"Hiragino Sans GB"',
          '"Microsoft YaHei"',
          'system-ui',
          'sans-serif'
        ]
      },
      boxShadow: {
        lift: '0 18px 50px rgba(0, 0, 0, 0.08)'
      }
    }
  }
}
