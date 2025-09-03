using AiRise.Models;
using System.Collections.Generic;

namespace AiRise.Data
{
    public static class ChallengeData
    {
        public static readonly List<Challenge> Challenges = new List<Challenge>
        {
            new Challenge { Id = "1", Name = "10,000 Steps Challenge", Description = "Walk 10,000 steps every day for a week.", Url = "https://pixabay.com/images/search/running%20training/" },
            new Challenge { Id = "2", Name = "Hydration Challenge", Description = "Drink 2 liters of water daily for 14 days.", Url = "https://www.shutterstock.com/image-photo/fit-slim-sportswoman-drinking-water-600nw-1333020713.jpg" },
            new Challenge { Id = "3", Name = "No Sugar Week", Description = "Avoid all added sugars for 7 days.", Url = "https://st2.depositphotos.com/1005563/8505/i/450/depositphotos_85057086-stock-photo-diet-and-fitness-vitamin-concept.jpg" },
            new Challenge { Id = "4", Name = "Morning Yoga", Description = "Do 20 minutes of yoga every morning for 10 days.", Url = "https://images.pexels.com/photos/3076509/pexels-photo-3076509.jpeg?cs=srgb&dl=pexels-jonathanborba-3076509.jpg&fm=jpg" },
            new Challenge { Id = "5", Name = "Plank Challenge", Description = "Hold a plank for 2 minutes daily for 7 days.", Url = "https://media.istockphoto.com/id/628092382/photo/its-great-for-the-abs.jpg?s=612x612&w=0&k=20&c=YOWaZRjuyh-OG6rv8k0quDNxRwqrxdMm8xgqe37Jmak=" },
            new Challenge { Id = "6", Name = "Push-Up Progression", Description = "Increase your push-ups by 5 each day for 10 days.", Url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQHx0OGGxDH11l_7OaTzUClXWPwCr2k9whIkQ&s" },
            new Challenge { Id = "7", Name = "Squat Streak", Description = "Do 50 squats every day for 14 days.", Url = "https://media.istockphoto.com/id/1149242776/photo/mature-strong-man-lifting-weights-at-cross-training.jpg?s=612x612&w=0&k=20&c=pqhlsg9QHdSccbjzL0aVTbELRibJj6levS9N7jKDHy0=" },
            new Challenge { Id = "8", Name = "Stretching Routine", Description = "Stretch for 15 minutes every evening for 2 weeks.", Url = "https://thumbs.dreamstime.com/b/body-stretch-fit-man-stretching-back-doing-yoga-workout-gym-black-male-athlete-doing-back-extension-stretch-exercise-lying-157467114.jpg" },
            new Challenge { Id = "9", Name = "Cardio Blast", Description = "Complete 30 minutes of cardio 5 times a week for 3 weeks.", Url = "https://media.istockphoto.com/id/1132086660/photo/side-view-of-beautiful-muscular-woman-running-on-treadmill.jpg?s=612x612&w=0&k=20&c=5Vq_BJjG7sbIyKIP-Adu0pChReDXm0dC7BVPvto2M0I=" },
            new Challenge { Id = "10", Name = "Core Crusher", Description = "Do a core workout every other day for 2 weeks.", Url = "https://www.shutterstock.com/image-photo/exercise-abdomen-training-man-fitness-600nw-2422946865.jpg" },
        };
    }
}
