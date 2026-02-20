// util.js - Utility functions for API and JSON handling

/**
 * Safely parse a fetch Response as JSON, or return raw text if parsing fails.
 * @param {Response} response
 * @returns {Promise<Object|string>} Parsed JSON or raw text
 */
const toJson = async (response) => {
    const text = await response.text();
    try {
        return JSON.parse(text);
    } catch (error) {
        return { raw: text };
    }
};

/**
 * Fetch from API and parse response as JSON, throwing on error status.
 * @param {string} path - API path (e.g. '/api/game')
 * @param {Object} options - fetch options
 * @returns {Promise<Object>} Parsed JSON response
 */
const apiFetch = async (path, options) => {
    const fullUrl = `${window.location.origin}${path}`;
    console.log("apiFetch - URL:", fullUrl, "options:", options);
    const response = await fetch(fullUrl, options);
    console.log("apiFetch - response status:", response.status, response.statusText);
    const data = await toJson(response);
    console.log("apiFetch - data:", data);
    if (!response.ok) {
        const message = data.message || data.error || response.statusText;
        console.error("apiFetch - error:", message);
        throw new Error(message);
    }
    return data;
};
