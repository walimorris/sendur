const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyPlugin = require('copy-webpack-plugin');
const { resolve } = require("@babel/core/lib/vendor/import-meta-resolve");

module.exports = {
    mode: 'development',
    devtool: 'inline-source-map',
    entry: path.resolve(__dirname, './frontend/src/index.js'), // Entry point
    output: {
        path: path.resolve(__dirname, './frontend/static/built'), // Output directory
        filename: 'bundle.js', // Output bundle filename
        publicPath: 'built/', // Public path for assets
    },
    module: {
        rules: [
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader',
                },
            },
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader'],
            },
            {
                test: /\.(png|svg|jpg|gif)$/,
                use: ['file-loader'],
            },
            {
                test: /\.md$/,
                use: [
                    {
                        loader: 'html-loader'
                    },
                    {
                        loader: 'markdown-loader'
                    }
                ]
            },
        ],
    },
    plugins: [
        new HtmlWebpackPlugin({
            template: path.resolve(__dirname, './frontend/public/index.html'), // HTML template file
            filename: 'index.html', // Output HTML filename
        }),
        new CopyPlugin({
            patterns: [
                { from: path.resolve(__dirname, './frontend/static/built/index.html'), to: path.resolve(__dirname, './backend/src/main/resources/static/built/index.html'), noErrorOnMissing: true },
                { from: path.resolve(__dirname, './frontend/static/built/bundle.js'), to: path.resolve(__dirname, './backend/src/main/resources/static/built/bundle.js'), noErrorOnMissing: true },
            ],
        }),
    ],
    devServer: {
        contentBase: path.resolve(__dirname, './frontend/static/built'), // Serve content from 'build' directory during development
        historyApiFallback: true, // Allows HTML5 history API based routing to work (for React Router)
    },
};


