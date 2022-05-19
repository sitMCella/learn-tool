import React, { useEffect, useState } from 'react'
import { ACCESS_TOKEN } from '../constants'
import { Redirect } from 'react-router-dom'

function OAuth2RedirectHandler (props) {
  const [token, setToken] = useState(null)
  const [error, setError] = useState(null)

  const getRegex = (name) => {
    return new RegExp('[\\?&]' + name + '=([^&#]*)')
  }

  const getValueFromUrlParameter = (name) => {
    const regex = getRegex(name)
    const results = regex.exec(props.location.search)
    return results === null ? '' : decodeURIComponent(results[1].replace(/\+/g, ' '))
  }

  useEffect(() => {
    const controller = new AbortController()
    const tokenValue = getValueFromUrlParameter('token')
    setToken(tokenValue)
    localStorage.setItem(ACCESS_TOKEN, tokenValue)
    const errorValue = getValueFromUrlParameter('error')
    setError(errorValue)
    return () => controller.abort()
  }, [])

  if (token) {
    return (
            <Redirect to={{
              pathname: '/',
              state: { from: props.location }
            }}/>
    )
  } else {
    return (
            <Redirect to={{
              pathname: '/login',
              state: {
                from: props.location,
                error: { error }
              }
            }}/>
    )
  }
}

export default OAuth2RedirectHandler
