// eslint-disable-next-line import/prefer-default-export
export const data = [
    {
        text: 'Basic info',
        content: {
            serviceId: {
                value: '',
                question: 'The id of your service',
            },
            title: {
                value: '',
                question: 'The title of your API',
            },
            description: {
                value: '',
                question: 'Short description of your API',
            },
            baseUrl: {
                value: '',
                question: 'Base URL of your API',
            },
        },
    },
    {
        text: 'URL',
        content: {
            homePageRelativeUrl: {
                value: '',
                question: 'Provide short description',
            },
            statusPageRelativeUrl: {
                value: '',
                question: 'Provide short description',
            },
            healthCheckRelativeUrl: {
                value: '',
                question: 'Provide short description',
            },
        },
    },
    {
        text: 'Discovery Service URL',
    },
    {
        text: 'Routes',
    },
    {
        text: 'API info',
    },
    {
        text: 'Catalog',
    },
    {
        text: 'SSL',
    },
];

export const enablerData = [
    {
        text: 'Plain Java Enabler',
        categories: [
            { name: 'Basic info', indentation: false },
            { name: 'URL', indentation: false },
            { name: 'Routes', indentation: 'routes' },
            { name: 'Catalog', indentation: 'catalog/tiles' },
            { name: 'SSL', indentation: 'ssl' },
        ],
    },
    {
        text: 'Spring Enabler',
    },
    {
        text: 'Micronaut Enabler',
    },
    {
        text: 'Node JS Enabler',
    },
    {
        text: 'Static Onboarding',
    },
    {
        text: 'Direct Call to Eureka',
    },
];