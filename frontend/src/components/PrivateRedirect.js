import React from 'react'
import { Redirect } from 'react-router-dom'

const PrivateRoute = ({ ...rest }) => (
    <Redirect exact from="/workspaces/:name" to="/workspaces/:name/cards" {...rest} />
)

export default PrivateRoute
