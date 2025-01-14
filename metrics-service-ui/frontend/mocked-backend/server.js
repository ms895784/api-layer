const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const routes = require('./routes').router;

const app = express();

const corsOptions = {
    origin: 'https://localhost:3000',
    credentials: true,
};

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(cors(corsOptions));

routes(app);

const server = app.listen(8000, () => {
    console.debug(`Mocked backend running on port - ${server.address().port}`);
});
