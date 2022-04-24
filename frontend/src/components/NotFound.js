import React from 'react'
import { Link } from 'react-router-dom'
import Box from '@material-ui/core/Box'
import CardUi from '@material-ui/core/Card'
import CardActions from '@material-ui/core/CardActions'
import CardContent from '@material-ui/core/CardContent'
import Typography from '@material-ui/core/Typography'
import HomeIcon from '@material-ui/icons/Home'
import Fab from '@material-ui/core/Fab'
import { makeStyles } from '@material-ui/core/styles'

function NotFound () {
  const useStyles = makeStyles(() => ({
    card: {
      width: '100%',
      textAlign: 'left'
    },
    cardTitle: {
      width: 80
    },
    actions: {
      display: 'flex',
      justifyContent: 'flex-end'
    },
    expand: {
      marginLeft: 'auto',
      marginTop: 'auto'
    }
  }))
  const classes = useStyles()

  return (
        <CardUi className={classes.card}>
            <CardContent>
                <Box display="flex" flexWrap="wrap" p={0} m={0}>
                    <Box pr={2} className={classes.cardTitle}>
                        <Typography variant="body1" color="textSecondary" component="p" gutterBottom>
                            Page not found
                        </Typography>
                    </Box>
                    <Box p={0}>
                        <Typography variant="body1" color="textSecondary" component="p" gutterBottom>
                            The page you are looking for was not found.
                        </Typography>
                    </Box>
                </Box>
                <CardActions className={classes.actions}>
                    <Fab size="small" color="primary" aria-label="add" component={Link} to={'/'}>
                        <HomeIcon />
                    </Fab>
                </CardActions>
            </CardContent>
        </CardUi>
  )
}

export default NotFound
